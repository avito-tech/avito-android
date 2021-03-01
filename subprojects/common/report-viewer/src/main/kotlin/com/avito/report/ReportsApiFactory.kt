package com.avito.report

import com.avito.http.RetryInterceptor
import com.avito.logger.Logger
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.internal.JsonRpcRequestProvider
import com.avito.report.internal.getHttpClient
import com.avito.report.model.EntryTypeAdapterFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder

object ReportsApiFactory {

    private const val READ_TIMEOUT = 60L
    private const val WRITE_TIMEOUT = 10L
    private const val VERBOSE_HTTP = false

    /**
     * for tests
     */
    internal val gson: Gson = GsonBuilder()
        .registerTypeAdapterFactory(EntryTypeAdapterFactory())
        .create()

    fun create(
        host: String,
        loggerFactory: LoggerFactory,
        readTimeout: Long = READ_TIMEOUT,
        writeTimeout: Long = WRITE_TIMEOUT,
        verboseHttp: Boolean = VERBOSE_HTTP,
        logger: Logger = loggerFactory.create<ReportsApi>(),
        retryInterceptor: RetryInterceptor? = RetryInterceptor(
            logger = logger,
            allowedMethods = listOf("POST")
        )
    ): ReportsApi {
        return ReportsApiImpl(
            loggerFactory = loggerFactory,
            requestProvider = JsonRpcRequestProvider(
                host = host,
                httpClient = getHttpClient(
                    verbose = verboseHttp,
                    logger = logger,
                    readTimeoutSec = readTimeout,
                    writeTimeoutSec = writeTimeout,
                    retryInterceptor = retryInterceptor
                ),
                gson = gson
            )
        )
    }
}
