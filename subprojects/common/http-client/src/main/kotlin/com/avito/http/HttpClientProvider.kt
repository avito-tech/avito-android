package com.avito.http

import com.avito.android.stats.CountMetric
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDSender
import com.avito.http.TryFailCallback.Companion.combine
import com.avito.http.internal.ServiceMetricsInterceptor
import com.avito.http.internal.StatsdServiceEventsListener
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

public class HttpClientProvider(
    private val statsDSender: StatsDSender,
    private val builderTransform: OkHttpClient.Builder.() -> OkHttpClient.Builder = { this }
) {

    private val builder = OkHttpClient.Builder()

    public fun provide(
        serviceName: String,
        timeoutMs: Long? = null,
        retryPolicy: RetryPolicy? = null,
        builderTransform: OkHttpClient.Builder.() -> OkHttpClient.Builder = { this }
    ): OkHttpClient {

        val seriesName = SeriesName.create("service", serviceName)

        return builder
            .also { this.builderTransform(it) }
            .also { builderTransform(it) }
            .apply {
                if (timeoutMs != null) {
                    readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                    writeTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                    connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                }
            }
            .apply {
                if (retryPolicy != null) {
                    val onTryFail = object : TryFailCallback {

                        override fun onTryFail(attemptNumber: Int, request: Request, exception: Throwable) {
                            statsDSender.send(CountMetric(seriesName.append("try-fail")))
                        }
                    }

                    addInterceptor(
                        RetryInterceptor(
                            retryPolicy.copy(
                                onTryFail = retryPolicy.onTryFail.combine(onTryFail)
                            )
                        )
                    )
                }
            }
            .addInterceptor(
                ServiceMetricsInterceptor(
                    StatsdServiceEventsListener(
                        statsDSender = statsDSender,
                        prefix = seriesName
                    )
                )
            )
            .build()
    }

    public companion object
}
