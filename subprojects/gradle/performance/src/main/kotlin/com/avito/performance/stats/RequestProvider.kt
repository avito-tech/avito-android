package com.avito.performance.stats

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

internal class RequestProvider(
    val url: String,
    val httpClient: OkHttpClient,
    val gson: Gson
) {

    val jsonMime = "application/json"

    inline fun <reified T : Any> request(method: String, request: Any): T {
        val response = httpClient.newCall(
            Request.Builder()
                .url(url.removeSuffix("/") + method)
                .post(gson.toJson(request).toRequestBody(jsonMime.toMediaType()))
                .build()
        ).execute()

        val body = response.body
        if (body != null && response.isSuccessful) {
            return gson.fromJson(body.string())
        } else {
            throw Exception("Request failed: ${response.message} $request\n${body?.string()}")
        }
    }
}
