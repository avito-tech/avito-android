package com.avito.performance.stats

import com.avito.http.RetryInterceptor
import com.avito.utils.logging.CILogger
import com.avito.utils.logging.commonLogger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

internal class HttpClientProvider(private val logger: CILogger) {

    fun getHttpClient(verbose: Boolean): OkHttpClient = OkHttpClient.Builder()
        .readTimeout(90, TimeUnit.SECONDS)
        .addInterceptor(
            RetryInterceptor(
                allowedMethods = listOf("POST", "GET"),
                logger = commonLogger(logger)
            )
        )
        .addInterceptor(
            HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    if (verbose) {
                        logger.debug(message)
                    }
                }
            }).setLevel(HttpLoggingInterceptor.Level.BASIC)
        )
        .build()
}
