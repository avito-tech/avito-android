package com.avito.report.internal

import com.avito.report.internal.model.RfcRpcRequest
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

internal class JsonRpcRequestProvider(
    private val host: String,
    private val httpClient: OkHttpClient,
    private val gson: Gson
) {

    private val jsonMime = "application/json"

    inline fun <reified T : Any> jsonRpcRequest(request: RfcRpcRequest): T = internalRequest(request)

    inline fun <reified T : Any> batchRequest(request: List<RfcRpcRequest>): T = internalRequest(request)

    private inline fun <reified Response : Any> internalRequest(request: Any): Response {
        val response = httpClient.newCall(
            Request.Builder()
                .url(host.removeSuffix("/"))
                .header("Accept", jsonMime)
                .header("Content-Type", jsonMime)
                .post(RequestBody.create(MediaType.get(jsonMime), gson.toJson(request)))
                .build()
        ).execute()

        val responseBody = response.body()?.string()
        if (responseBody != null && response.isSuccessful) {
            return gson.fromJson(responseBody)
        } else {
            throw Exception("Request failed: ${response.message()} ${responseBody}\n$request")
        }
    }
}
