package com.avito.report.internal

import com.avito.http.FallbackInterceptor
import com.avito.http.RetryInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import java.util.concurrent.TimeUnit

internal fun getHttpClient(verbose: Boolean, fallbackUrl: String, logger: (String, Throwable?) -> Unit): OkHttpClient =
    OkHttpClient.Builder()
        .readTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(RetryInterceptor(logger = { message, error -> logger.invoke(message, error) }))
        .addInterceptor(
            FallbackInterceptor(
                fallbackRequest = { request ->
                    request.newBuilder()
                        .url(fallbackUrl)
                        .build()
                },
                onFallback = { logger.invoke("Fallback to ingress", null) })
        )
        .apply {
            addInterceptor(
                HttpLoggingInterceptor { logger.invoke(it, null) }
                    .setLevel(if (verbose) Level.BODY else Level.BASIC)
            )
        }
        .build()
