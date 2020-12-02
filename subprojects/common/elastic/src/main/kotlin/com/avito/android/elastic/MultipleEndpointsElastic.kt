package com.avito.android.elastic

import com.avito.time.TimeProvider
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_DATE
import java.util.concurrent.TimeUnit.SECONDS

/**
 * Multiple endpoints used for stability, sometimes nodes may be unresponsive
 * todo retries on other nodes (or it may be even less stable in corner cases)
 *
 * @param endpoints list of elastic endpoints to send logs
 * @param indexPattern see https://www.elastic.co/guide/en/kibana/current/index-patterns.html
 * @param verboseHttpLog verbose http for debugging purposes
 * @param onError can't deliver message; reaction delegated to upstream
 */
class MultipleEndpointsElastic(
    private val timeProvider: TimeProvider,
    private val endpoints: List<String>,
    private val indexPattern: String,
    private val buildId: String,
    private val verboseHttpLog: ((String) -> Unit)?,
    private val onError: (String, Throwable?) -> Unit
) : Elastic {

    private val defaultTimeoutSec = 10L

    private val gson = GsonBuilder().create()

    private val elasticApi: RoundRobinIterable<Lazy<ElasticLogApi>> =
        RoundRobinIterable(endpoints.map { endpoint -> lazy { createApiService(endpoint) } })

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

            elasticApi.next().value.log(
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
            ).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (!response.isSuccessful) {
                        onError("Can't send logs to Elastic, response code: ${response.code()}", null)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    onError("Can't send logs to Elastic", t)
                }
            })
        } catch (e: Throwable) {
            onError("Can't send logs to Elastic", e)
        }
    }

    private fun createApiService(endpoint: String): ElasticLogApi {
        val httpClientBuilder = OkHttpClient()
            .newBuilder()
            .connectTimeout(defaultTimeoutSec, SECONDS)
            .readTimeout(defaultTimeoutSec, SECONDS)

        if (verboseHttpLog != null) {
            httpClientBuilder.addInterceptor(
                HttpLoggingInterceptor(HttpSystemLogger(verboseHttpLog)).apply { level = BODY }
            )
        }

        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClientBuilder.build())
            .baseUrl(endpoint)
            .build()
            .create()
    }

    private class HttpSystemLogger(
        private val httpLog: ((String) -> Unit)
    ) : HttpLoggingInterceptor.Logger {

        override fun log(message: String) = httpLog(message)
    }
}
