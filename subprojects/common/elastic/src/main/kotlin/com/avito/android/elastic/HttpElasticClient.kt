package com.avito.android.elastic

import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.time.TimeProvider
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL

/**
 * @param endpoints list of elastic endpoints to send logs
 *                  multiple endpoints used for stability, sometimes nodes may be unresponsive
 * @param indexPattern see https://www.elastic.co/guide/en/kibana/current/index-patterns.html
 */
internal class HttpElasticClient(
    private val timeProvider: TimeProvider,
    private val endpoints: List<URL>,
    private val indexPattern: String,
    private val buildId: String,
    loggerFactory: LoggerFactory
) : ElasticClient {

    private val logger = loggerFactory.create<HttpElasticClient>()

    private val elasticServiceFactory = ElasticServiceFactory

    private val elasticApi = elasticServiceFactory.create(
        endpoints = endpoints.toHttpUrls(),
        loggerFactory = loggerFactory
    ).provide()

    private val timestampFormatter = timeProvider.formatter("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSZ")

    private val isoDate = timeProvider.formatter("yyyy-MM-dd")

    override fun sendMessage(
        level: String,
        message: String,
        metadata: Map<String, String>,
        throwable: Throwable?
    ) {
        try {
            val nowWithTimeZone = timeProvider.now()

            val params = mutableMapOf(
                "@timestamp" to timestampFormatter.format(nowWithTimeZone),
                "level" to level,
                "build_id" to buildId,
                "message" to message
            )

            val errorMessage = throwable?.message
            if (!errorMessage.isNullOrBlank()) {
                params["error_message"] = errorMessage
            }

            params.putAll(metadata)

            elasticApi.log(
                indexPattern = indexPattern,
                date = isoDate.format(nowWithTimeZone),
                params = params
            ).enqueue(
                object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        logger.warn("Can't send logs to Elastic", t)
                    }
                }
            )
        } catch (t: Throwable) {
            logger.warn("Can't send logs to Elastic", t)
        }
    }

    private fun List<URL>.toHttpUrls(): List<HttpUrl> = mapNotNull { url ->
        val result = url.toHttpUrlOrNull()
        if (result == null) {
            logger.warn("Can't convert URL to okhttp.HttpUrl: $url")
        }
        result
    }
}
