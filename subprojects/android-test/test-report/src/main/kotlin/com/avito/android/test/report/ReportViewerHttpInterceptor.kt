package com.avito.android.test.report

import com.avito.filestorage.RemoteStorage
import com.avito.http.internal.isPlaintext
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.GsonBuilder
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.internal.http.promisesBody
import okio.Buffer
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

/**
 * Intercepts http requests with side-effect: append log entry to current test step
 */
class ReportViewerHttpInterceptor(
    private val reportProvider: ReportProvider,
    private val remoteFileStorageEndpointHost: String
) : Interceptor {

    private val report: Report by lazy { reportProvider.report }

    private val prettifier = GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // чтобы не улететь в stackoverflow
        if (RemoteStorage.isFileStorageHost(request.url)) return chain.proceed(request)

        val reportViewerMessage = StringBuilder()
        reportViewerMessage.appendLine(request.printUrl())
        reportViewerMessage.appendLine(request.printHeaders())
        reportViewerMessage.appendLine(request.printBody())

        val label = "HTTP ${request.method}: ${request.url.toString().split("?")[0]}"

        val startNs = System.nanoTime()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            reportViewerMessage.appendLine("<-- HTTP FAILED: $e")
            report.addText("$label    HTTP FAILED!!!", reportViewerMessage.toString())
            throw e
        }

        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

        reportViewerMessage.appendLine(response.printUrl(tookMs))
        reportViewerMessage.appendLine(response.printHeaders())
        reportViewerMessage.appendLine(response.printBody())

        report.addText(label + "    ${response.code}", reportViewerMessage.toString())

        return response
    }

    private fun Request.printUrl(): String {
        val result = StringBuffer()
        result.append("--> $method $url")
        val requestBody = body
        if (requestBody != null) {
            result.append("(${requestBody.contentLength()}-byte body)")
        }
        return result.toString()
    }

    private fun Request.printHeaders(): String {
        val result = StringBuilder()

        val requestBody = body
        // Request body headers are only present when installed as a network interceptor. Force
        // them to be included (when available) so there values are known.
        if (requestBody != null) {
            if (requestBody.contentType() != null) {
                result.appendLine("Content-Type: ${requestBody.contentType()}")
            }
            if (requestBody.contentLength() != -1L) {
                result.appendLine("Content-Length: ${requestBody.contentLength()}")
            }
        }

        var requestHeadersIndex = 0
        val headers = headers
        val requestHeadersCount = headers.size
        while (requestHeadersIndex < requestHeadersCount) {
            val name = headers.name(requestHeadersIndex)
            // Skip headers from the request body as they are explicitly logged above.
            if (!"Content-Type".equals(name, ignoreCase = true) && !"Content-Length".equals(name, ignoreCase = true)) {
                result.appendLine(name + ": " + headers.value(requestHeadersIndex))
            }
            requestHeadersIndex++
        }
        return result.toString()
    }

    private fun Request.printBody(): String {
        val result = StringBuilder()
        val requestBody = body
        if (requestBody == null) {
            result.appendLine("--> END $method")
        } else if (bodyEncoded(headers)) {
            result.appendLine("--> END $method (encoded body omitted)")
        } else {
            val buffer = Buffer()
            requestBody.writeTo(buffer)

            val charset = determineBodyCharset(requestBody.contentType())

            if (buffer.isPlaintext()) {
                result.appendLine(tryPrettify(buffer.readString(charset)))
                result.appendLine("--> END $method (${requestBody.contentLength()}-byte body)")
            } else {
                result.appendLine("--> END $method (binary ${requestBody.contentLength()}-byte body omitted)")
            }
        }
        return result.toString()
    }

    private fun Response.printUrl(tookMs: Long): String {
        val message = if (message.isEmpty()) "" else " $message"
        return "<-- $code$message ${request.url} (${tookMs}ms)"
    }

    private fun Response.printHeaders(): String {
        val result = StringBuilder()
        val headers = headers
        var i = 0
        val count = headers.size
        while (i < count) {
            result.appendLine("${headers.name(i)}: ${headers.value(i)}")
            i++
        }
        return result.toString()
    }

    private fun Response.printBody(): String {
        val result = StringBuilder()
        val responseBody: ResponseBody? = body
        val contentLength = responseBody!!.contentLength()

        if (!promisesBody()) {
            result.appendLine("<-- END HTTP")
        } else if (bodyEncoded(headers)) {
            result.appendLine("<-- END HTTP (encoded body omitted)")
        } else {
            val source = responseBody.source()
            source.request(java.lang.Long.MAX_VALUE) // Buffer the entire body.
            val buffer = source.buffer

            val charset = determineBodyCharset(responseBody.contentType())

            if (!buffer.isPlaintext()) {
                result.appendLine("")
                result.appendLine("<-- END HTTP (binary ${buffer.size}-byte body omitted)")
                return result.toString()
            }

            if (contentLength != 0L) {
                result.appendLine(tryPrettify(buffer.clone().readString(charset)))
            }

            result.appendLine("<-- END HTTP (${buffer.size}-byte body)")
        }
        return result.toString()
    }

    private fun determineBodyCharset(
        mediaType: MediaType?,
        default: Charset = Charset.forName("UTF-8")
    ): Charset {
        return mediaType?.charset(default) ?: default
    }

    private fun RemoteStorage.Companion.isFileStorageHost(url: HttpUrl): Boolean =
        url.host == remoteFileStorageEndpointHost

    private fun tryPrettify(source: String): String {
        return try {
            prettifier.toJson(prettifier.fromJson(source))
        } catch (e: Exception) {
            source
        }
    }

    private fun bodyEncoded(headers: Headers): Boolean {
        val contentEncoding = headers["Content-Encoding"]
        return contentEncoding != null && !contentEncoding.equals("identity", ignoreCase = true)
    }
}
