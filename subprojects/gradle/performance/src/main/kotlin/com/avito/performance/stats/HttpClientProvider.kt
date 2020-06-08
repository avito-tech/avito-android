package com.avito.performance.stats

import com.avito.http.RetryInterceptor
import com.avito.logger.Logger
import com.avito.utils.logging.CILogger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

internal class HttpClientProvider(private val logger: CILogger) {

    fun getHttpClient(verbose: Boolean): OkHttpClient = OkHttpClient.Builder()
        .readTimeout(90, TimeUnit.SECONDS)
        .addInterceptor(
            RetryInterceptor(
                allowedMethods = listOf("POST", "GET"),
                logger = object : Logger {
                    override fun debug(msg: String) {
                        logger.debug(msg)
                    }

                    override fun exception(msg: String, error: Throwable) {
                        logger.debug(msg, error)
                    }

                    override fun critical(msg: String, error: Throwable) {
                        logger.debug(msg, error)
                    }
                }
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
