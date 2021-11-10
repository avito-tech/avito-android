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
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

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
        endpoints = endpoints.toHttpUrls()
    ).provide()

    private val timestampFormatter = utcFormatter("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

    private val isoDate = utcFormatter("yyyy-MM-dd")

    override fun sendMessage(
        level: String,
        message: String,
        metadata: Map<String, String>,
        throwable: Throwable?
    ) {
        try {
            val now = timeProvider.now()

            val params = mutableMapOf(
                "@timestamp" to timestampFormatter.get().format(now),
                "level" to level,
                "build_id" to buildId,
                "message" to message
            )

            val errorMessage = throwable?.message
            if (!errorMessage.isNullOrBlank()) {
                params["error_message"] = errorMessage
            }

            params.putAll(metadata)

            val formattedDate = isoDate.get().format(now)

            elasticApi.log(
                indexPattern = indexPattern,
                date = formattedDate,
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

    /**
     * from javadoc:
     *
     * It is recommended to create separate format instances for each thread.
     * If multiple threads access a format concurrently, it must be synchronized
     * externally.
     */
    private fun utcFormatter(pattern: String) = object : ThreadLocal<DateFormat>() {
        override fun initialValue(): DateFormat = SimpleDateFormat(pattern, Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
}
