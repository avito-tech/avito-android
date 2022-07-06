package com.avito.emcee.client.di.internal

import com.avito.android.stats.StatsDConfig
import com.avito.android.stats.StatsDSender
import com.avito.http.HttpClientProvider
import com.avito.logger.LoggerFactory
import com.avito.time.DefaultTimeProvider
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

internal class HttpClientProvider(
    private val loggerFactory: LoggerFactory,
) {

    private val client: OkHttpClient by lazy {
        val logger = loggerFactory.create("HTTP")
        val httpLoggingInterceptor = HttpLoggingInterceptor { message ->
            logger.debug(message)
        }
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        HttpClientProvider(
            statsDSender = StatsDSender.create(
                // todo do we need it here?
                config = StatsDConfig.Disabled,
                loggerFactory = loggerFactory
            ),
            timeProvider = DefaultTimeProvider(),
            loggerFactory = loggerFactory
        ).provide()
            .addInterceptor(httpLoggingInterceptor)
            .build()
    }

    fun provide(): OkHttpClient = client
}
