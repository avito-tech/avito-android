package com.avito.http

import com.avito.android.stats.StatsDSender
import com.avito.http.TryFailCallback.Companion.combine
import com.avito.http.internal.ServiceMetricsInterceptor
import com.avito.http.internal.StatsdServiceEventsListener
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.time.TimeProvider
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

public class HttpClientProvider(
    private val statsDSender: StatsDSender,
    private val loggerFactory: LoggerFactory,
    private val timeProvider: TimeProvider
) {

    private var builderTransform: (OkHttpClient.Builder.() -> OkHttpClient.Builder)? = null

    private val builder = OkHttpClient.Builder()

    /**
     * for testing only
     */
    internal constructor(
        statsDSender: StatsDSender,
        loggerFactory: LoggerFactory,
        timeProvider: TimeProvider,
        builderTransform: OkHttpClient.Builder.() -> OkHttpClient.Builder
    ) : this(statsDSender, loggerFactory, timeProvider) {
        this.builderTransform = builderTransform
    }

    /**
     * @param metadataInterceptor null if RequestMetadata set manually (see JsonRpcRequestProvider)
     */
    public fun provide(
        timeoutMs: Long? = null,
        retryPolicy: RetryPolicy? = null,
        metadataInterceptor: RequestMetadataInterceptor?
    ): OkHttpClient.Builder {

        return builder
            .apply {
                if (metadataInterceptor != null) {
                    addInterceptor(metadataInterceptor)
                }
            }
            .apply { builderTransform?.invoke(this) }
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
                        }
                    }

                    addInterceptor(
                        RetryInterceptor(
                            policy = retryPolicy.copy(
                                onTryFail = retryPolicy.onTryFail.combine(onTryFail)
                            ),
                            logger = loggerFactory.create<HttpClientProvider>()
                        )
                    )
                }
            }
            .addInterceptor(
                ServiceMetricsInterceptor(
                    StatsdServiceEventsListener(statsDSender = statsDSender),
                    timeProvider
                )
            )
    }

    public companion object
}
