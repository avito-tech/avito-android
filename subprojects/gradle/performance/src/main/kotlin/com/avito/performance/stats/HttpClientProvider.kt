package com.avito.performance.stats

import com.avito.http.RetryInterceptor
import com.avito.logger.Logger
import com.avito.utils.logging.CILogger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

internal class HttpClientProvider(
    private val logger: CILogger
) {

    fun getHttpClient(verbose: Boolean): OkHttpClient = OkHttpClient.Builder()
        .readTimeout(90, TimeUnit.SECONDS)
        .addInterceptor(
            RetryInterceptor(
                allowedMethods = listOf("POST", "GET"),
                logger = object : Logger {
                    override fun debug(msg: String) {
                        logger.info(msg)
                    }

                    override fun exception(msg: String, error: Throwable) {
                        logger.info(msg, error)
                    }

                    override fun critical(msg: String, error: Throwable) {
                        logger.info(msg, error)
                    }
                }
            )
        )
        .addInterceptor(
            HttpLoggingInterceptor {
                if (verbose) {
                    logger.debug(it)
                }
            }
                .setLevel(HttpLoggingInterceptor.Level.BASIC)
        )
        .build()
}
