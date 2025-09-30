package com.avito.android.instant_feedback.internal.client

import com.avito.logger.LoggerFactory
import com.avito.logger.create
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.nio.charset.Charset

internal class FeedbackClientImpl(
    private val url: String,
    private val json: Json,
    private val okHttpClient: OkHttpClient,
    loggerFactory: LoggerFactory,
) : FeedbackClient {

    private val logger = loggerFactory.create<FeedbackClient>()

    override suspend fun sendRequest(data: RequestFeedbackData) = withContext(Dispatchers.IO) {
        val jsonBody = json.encodeToString(data)
        val mediaType = "application/json".toMediaType()

        val requestBody = jsonBody.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            when {
                response.isSuccessful -> logger.info("Feedback request sent with payload: $jsonBody")
                else -> {
                    val bodyText = response.body?.byteString()?.string(Charset.defaultCharset())
                    logger.warn("Can't request feedback, code=${response.code}, body=$bodyText")
                }
            }
        }
    }
}
