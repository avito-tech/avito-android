package com.avito.test.summary.time

import com.avito.android.Result
import com.avito.http.RetryInterceptor
import com.google.gson.JsonObject
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Posts to Time through its Mattermost-compatible API v4.
 */
internal class TimeMessengerClient(
    baseUrl: String,
    private val token: String,
) : TimeMessenger {

    private val postsUrl: HttpUrl = baseUrl.toHttpUrl().newBuilder()
        .addPathSegments("api/v4/posts")
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            // The gateway answers 429 already at a few requests per second and gives no Retry-After.
            // Other codes are not retried: the post may have been created and would be duplicated.
            RetryInterceptor(
                allowedMethods = listOf("POST"),
                allowedCodes = listOf(TOO_MANY_REQUESTS),
            )
        )
        .build()

    override fun createPost(channelId: String, message: String): Result<Unit> = Result.tryCatch {
        val body = JsonObject().apply {
            addProperty("channel_id", channelId)
            addProperty("message", message)
        }
        val request = Request.Builder()
            .url(postsUrl)
            .header("Authorization", "Bearer $token")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            check(response.isSuccessful) {
                "Time API responded with ${response.code} to create post: ${response.body?.string()}"
            }
        }
    }

    private companion object {
        const val TOO_MANY_REQUESTS = 429
    }
}
