package com.avito.android.elastic

import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.time.TimeProvider
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * @param endpoints list of elastic endpoints to send logs
 *                  multiple endpoints used for stability, sometimes nodes may be unresponsive
 * @param indexName https://www.elastic.co/blog/what-is-an-elasticsearch-index
 * @param authApiKey API key for Elastic auth
 * see https://www.elastic.co/guide/en/elasticsearch/reference/current/token-authentication-services.html
 */
internal class HttpElasticClient(
    private val elasticApi: ElasticApi,
    private val timeProvider: TimeProvider,
    private val indexName: String,
    /**
     * e.g. worker, client, android-test-runtime
     */
    private val sourceType: String,
    /**
     * e.g. buildId, workerId
     */
    private val sourceId: String,
    private val authApiKey: String?,
    loggerFactory: LoggerFactory
) : ElasticClient {

    private val logger = loggerFactory.create<HttpElasticClient>()

    private val timestampFormatter = utcFormatter("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

    private val isoDate = utcFormatter("yyyy.MM.dd")

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
                "source.type" to sourceType,
                "source.id" to sourceId,
                "message" to message
            )

            val errorMessage = throwable?.message
            if (!errorMessage.isNullOrBlank()) {
                params["error_message"] = errorMessage
            }

            params.putAll(metadata)

            val authApiKeyHeaderValue = authApiKey?.let { "ApiKey $it" }

            val indexDate = isoDate.get().format(now)

            elasticApi.sendDocument(
                authApiKeyHeaderValue = authApiKeyHeaderValue,
                indexPattern = indexName,
                date = indexDate,
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
