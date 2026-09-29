package com.avito.android.feedback.internal

import com.avito.android.Result
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.Duration

internal class FeedbackSender(
    private val endpoint: String,
) {

    private val json = Json {
        encodeDefaults = true
    }

    private val client = OkHttpClient.Builder()
        .callTimeout(TIMEOUT)
        .connectTimeout(TIMEOUT)
        .readTimeout(TIMEOUT)
        .writeTimeout(TIMEOUT)
        .retryOnConnectionFailure(false)
        .build()

    fun encode(data: RequestFeedbackData): String = json.encodeToString(data)

    fun send(data: RequestFeedbackData): Result<Unit> = Result.tryCatch {
        val request = Request.Builder()
            .url(endpoint)
            .post(encode(data).toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException(
                    "Instant feedback endpoint $endpoint responded with " +
                        "${response.code}: ${response.body?.string()}"
                )
            }
        }
    }

    private companion object {

        private val TIMEOUT: Duration = Duration.ofSeconds(3)

        private val jsonMediaType = "application/json".toMediaType()
    }
}
