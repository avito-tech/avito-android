package com.avito.android.runner

import com.avito.http.HttpLogger
import com.avito.http.RetryInterceptor
import com.avito.logger.Logger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

internal fun createReportHttpClient(logger: Logger): OkHttpClient {
    val retryInterceptor = RetryInterceptor(
        allowedMethods = listOf("GET", "POST"),
        logger = logger
    )
    val httpLoggingInterceptor = HttpLoggingInterceptor(HttpLogger(logger)).apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    return OkHttpClient.Builder()
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(retryInterceptor)
        .addInterceptor(httpLoggingInterceptor)
        .build()
}

private const val TIMEOUT_SECONDS = 30L
