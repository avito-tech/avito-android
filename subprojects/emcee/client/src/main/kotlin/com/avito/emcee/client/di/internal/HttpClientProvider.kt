package com.avito.emcee.client.di.internal

import com.avito.logger.LoggerFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

internal class HttpClientProvider(
    private val loggerFactory: LoggerFactory,
) {

    private val client: OkHttpClient by lazy {
        val logger = loggerFactory.create("HTTP")
        val httpLoggingInterceptor = HttpLoggingInterceptor { message ->
            logger.verbose(message)
        }
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .build()
    }

    fun provide(): OkHttpClient = client
}
