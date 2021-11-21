package com.avito.android.signer.internal

import com.avito.android.Result
import com.avito.http.internal.RequestMetadata
import com.avito.utils.ExistingFile
import com.avito.utils.createOrClear
import com.avito.utils.toExisting
import okhttp3.HttpUrl
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.File

internal class SignViaServiceAction(
    private val serviceUrl: HttpUrl,
    private val httpClient: OkHttpClient,
    private val token: String,
    private val unsignedFile: File,
    private val signedFile: File,
) {

    fun sign(): Result<ExistingFile> = Result.tryCatch {
        val request = buildRequest()

        val response = httpClient
            .newCall(request)
            .execute()

        writeResponse(
            request = request,
            response = response,
            outputFile = signedFile
        )

        signedFile.toExisting()
    }

    private fun buildRequest(): Request {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addPart(
                MultipartBody.Part.createFormData("token", token)
            )
            .addPart(
                MultipartBody.Part.createFormData(
                    "files",
                    unsignedFile.name,
                    unsignedFile.asRequestBody(null)
                )
            )
            .build()

        return Request.Builder()
            .url(serviceUrl)
            .post(body)
            .tag(
                RequestMetadata::class.java,
                RequestMetadata("signer", "sign")
            )
            .build()
    }

    private fun writeResponse(request: Request, response: Response, outputFile: File) {
        if (response.isSuccessful) {
            outputFile.createOrClear()
            response.body
                ?.byteStream()
                ?.use { input ->
                    outputFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
        } else {
            val stringBody = response.body?.string()

            val errorMessage = buildString {
                appendLine("Failed to sign $unsignedFile via service")
                appendLine("Request: ${request.method} ${request.url}")
                if (request.headers.size > 0) {
                    appendLine("Request headers:")
                    append(request.headers)
                }
                val requestBody = request.body
                if (requestBody != null) {
                    if (requestBody.contentLength() != -1L) {
                        if (request.headers["Content-Length"] == null) {
                            appendLine("Request body size: ${requestBody.contentLength()} bytes")
                        }
                    }
                }
                appendLine("Response: ${response.code}")
                if (response.headers.size > 0) {
                    appendLine("Response headers:")
                    append(response.headers)
                }
                if (stringBody.isNullOrBlank()) {
                    appendLine("Response body is empty")
                } else {
                    appendLine("Response body: $stringBody")
                }
            }

            error(errorMessage)
        }
    }
}
