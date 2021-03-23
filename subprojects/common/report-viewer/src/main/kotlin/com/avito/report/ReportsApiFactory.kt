package com.avito.report

import com.avito.http.HttpLogger
import com.avito.http.RetryInterceptor
import com.avito.logger.Logger
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.internal.JsonRpcRequestProvider
import com.avito.report.model.EntryTypeAdapterFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object ReportsApiFactory {

    private const val TIMEOUT_SEC = 30L

    /**
     * for tests
     */
    internal val gson: Gson = GsonBuilder()
        .registerTypeAdapterFactory(EntryTypeAdapterFactory())
        .create()

    fun create(
        host: String,
        loggerFactory: LoggerFactory,
        logger: Logger = loggerFactory.create<ReportsApi>(),
        retryInterceptor: RetryInterceptor? = RetryInterceptor(
            allowedMethods = listOf("POST")
        )
    ): ReportsApi {
        return ReportsApiImpl(
            loggerFactory = loggerFactory,
            requestProvider = JsonRpcRequestProvider(
                host = host,
                httpClient = getHttpClient(
                    verbose = false, // do not enable for production, generates a ton of logs
                    logger = logger,
                    retryInterceptor = retryInterceptor
                ),
                gson = gson
            )
        )
    }

    // for tests
    internal fun Request.describeJsonRpc(): String = "${url.redact()} method: ${tag()}"

    private fun getHttpClient(
        verbose: Boolean,
        logger: Logger,
        retryInterceptor: RetryInterceptor?
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
            .connectTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
            .apply {
                if (verbose) {
                    addInterceptor(
                        HttpLoggingInterceptor(HttpLogger(logger))
                            .setLevel(HttpLoggingInterceptor.Level.BODY)
                    )
                }
                if (retryInterceptor != null) {
                    addInterceptor(retryInterceptor)
                }
            }
            .build()
    }
}
