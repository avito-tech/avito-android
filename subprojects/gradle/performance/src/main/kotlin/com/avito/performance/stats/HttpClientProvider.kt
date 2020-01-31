package com.avito.performance.stats

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
            HttpLoggingInterceptor {
                if (verbose) {
                    logger.debug(it)
                }
            }
                .setLevel(HttpLoggingInterceptor.Level.BASIC)
        )
        .build()
}
