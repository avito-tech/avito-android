package com.avito.report.internal

import com.avito.http.FallbackInterceptor
import com.avito.http.RetryInterceptor
import com.avito.logger.Logger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import java.util.concurrent.TimeUnit

internal fun getHttpClient(
    verbose: Boolean,
    fallbackUrl: String,
    logger: Logger,
    readTimeout: Long,
    writeTimeout: Long
): OkHttpClient =
    OkHttpClient.Builder()
        .readTimeout(readTimeout, TimeUnit.SECONDS)
        .writeTimeout(writeTimeout, TimeUnit.SECONDS)
        .addInterceptor(RetryInterceptor(logger = logger))
        .addInterceptor(
            FallbackInterceptor(
                fallbackRequest = { request ->
                    request.newBuilder()
                        .url(fallbackUrl)
                        .build()
                },
                onFallback = { logger.debug("Fallback to ingress") })
        )
        .apply {
            if (verbose) {
                addInterceptor(HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                    override fun log(message: String) {
                        logger.debug(message)
                    }
                }).setLevel(Level.BODY))
            }
        }
        .build()
