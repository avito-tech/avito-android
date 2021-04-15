package com.avito.report.internal

import com.avito.http.internal.RequestMetadata
import com.avito.report.internal.model.RfcRpcRequest
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

internal class JsonRpcClient(
    private val host: String,
    private val httpClient: OkHttpClient,
    private val gson: Gson
) {

    private val jsonMime = "application/json"

    inline fun <reified T : Any> jsonRpcRequest(request: RfcRpcRequest): T = internalRequest(request)

    inline fun <reified T : Any> batchRequest(request: List<RfcRpcRequest>): T = internalRequest(request)

    private inline fun <reified Response : Any> internalRequest(jsonRpcRequest: Any): Response {

        val httpRequest = Request.Builder()
            .url(host.removeSuffix("/"))
            .header("Accept", jsonMime)
            .header("Content-Type", jsonMime)
            .post(gson.toJson(jsonRpcRequest).toRequestBody(jsonMime.toMediaType()))

        val jsonRpcMethod = getMethod(jsonRpcRequest)

        httpRequest.tag(
            type = RequestMetadata::class.java,
            tag = RequestMetadata(serviceName = "reports", methodName = jsonRpcMethod)
        )

        val response = httpClient.newCall(httpRequest.build()).execute()

        val responseBody = response.body?.string()
        if (responseBody != null && response.isSuccessful) {
            return try {
                gson.fromJson(responseBody)
            } catch (e: Throwable) {
                throw IllegalStateException("Can't parse response body", e)
            }
        } else {
            val failedRequestDescription = """
                |Failed request: $host method:$jsonRpcMethod
                |Response: $response
                |Response body: $responseBody
                |Response headers: ${response.headers}
                """.trimMargin()
            throw IOException(failedRequestDescription)
        }
    }

    private fun getMethod(request: Any): String = when (request) {
        is RfcRpcRequest -> request.method
        is List<*> -> {
            val methods = request.filterIsInstance<RfcRpcRequest>().map { it.method }.distinct()
            when (methods.size) {
                0 -> throw IllegalArgumentException("Trying to send empty or non json rpc requests")
                1 -> methods.first()
                else -> "multiple-methods"
            }
        }
        else -> "unknown"
    }
}
