package com.avito.report.internal

import com.avito.http.HttpLogger
import com.avito.http.RetryInterceptor
import com.avito.logger.Logger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import java.util.concurrent.TimeUnit

fun getHttpClient(
    verbose: Boolean = false,
    logger: Logger,
    readTimeoutSec: Long,
    writeTimeoutSec: Long
): OkHttpClient {

    return OkHttpClient.Builder()
        .readTimeout(readTimeoutSec, TimeUnit.SECONDS)
        .writeTimeout(writeTimeoutSec, TimeUnit.SECONDS)
        .addInterceptor(RetryInterceptor(logger = logger))
        .apply {
            if (verbose) {
                addInterceptor(HttpLoggingInterceptor(HttpLogger(logger)).setLevel(Level.BODY))
            }
        }
        .build()
}
