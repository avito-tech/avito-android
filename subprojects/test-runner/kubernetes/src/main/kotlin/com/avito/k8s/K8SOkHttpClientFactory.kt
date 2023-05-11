package com.avito.k8s

import com.avito.android.stats.StatsDSender
import com.avito.http.RetryInterceptor
import com.avito.http.StatsHttpEventListener
import com.avito.logger.LoggerFactory
import com.avito.time.TimeProvider
import io.fabric8.kubernetes.client.okhttp.OkHttpClientFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

public class K8SOkHttpClientFactory(
    private val loggerFactory: LoggerFactory,
    private val timeProvider: TimeProvider,
    private val statsDSender: StatsDSender,
    private val httpTries: Int,
) : OkHttpClientFactory() {

    override fun additionalConfig(builder: OkHttpClient.Builder) {
        super.additionalConfig(builder)
        val logger = loggerFactory.create("KubernetesHttpClient")
        builder
            .addInterceptor(RetryInterceptor(tries = httpTries))
            .addInterceptor(HttpLoggingInterceptor(logger::debug).apply { level = HttpLoggingInterceptor.Level.BODY })
            .eventListenerFactory {
                StatsHttpEventListener(
                    statsDSender = statsDSender,
                    timeProvider = timeProvider,
                    requestMetadataProvider = KubernetesRequestMetadataProvider(),
                    loggerFactory = loggerFactory,
                )
            }
    }
}
