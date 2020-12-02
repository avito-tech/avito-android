package com.avito.android.elastic

import com.avito.time.TimeProvider
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_DATE

/**
 * @param endpoint elastic endpoints to send logs
 * @param indexPattern see https://www.elastic.co/guide/en/kibana/current/index-patterns.html
 * @param onError can't deliver message; reaction delegated to upstream
 */
class HttpElasticClient(
    okHttpClient: OkHttpClient,
    private val timeProvider: TimeProvider,
    private val endpoint: String,
    private val indexPattern: String,
    private val buildId: String,
    private val onError: (String, Throwable?) -> Unit
) : ElasticClient {

    private val elasticServiceFactory = ElasticServiceFactory(okHttpClient)

    private val elasticApi = elasticServiceFactory.createApiService(endpoint)

    private val timezone = ZoneId.of("Europe/Moscow")

    private val timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSZ")

    override fun sendMessage(
        tag: String,
        level: String,
        message: String,
        throwable: Throwable?
    ) {
        try {
            val nowWithTimeZone = ZonedDateTime.ofInstant(timeProvider.now().toInstant(), timezone)
            val nowWithoutTimeZone = LocalDateTime.ofInstant(timeProvider.now().toInstant(), timezone)

            elasticApi.log(
                indexPattern = indexPattern,
                date = nowWithoutTimeZone.format(ISO_DATE),
                logEvent = ElasticLogEventRequest(
                    timestamp = nowWithTimeZone.format(timestampFormatter),
                    tag = tag,
                    level = level,
                    buildId = buildId,
                    message = message,
                    errorMessage = throwable?.message
                )
            ).enqueue(
                object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        onError("Can't send logs to Elastic", t)
                    }
                }
            )
        } catch (t: Throwable) {
            onError("Can't send logs to Elastic", t)
        }
    }
}
