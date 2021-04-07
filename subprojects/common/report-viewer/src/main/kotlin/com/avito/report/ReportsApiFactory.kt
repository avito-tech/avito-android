package com.avito.report

import com.avito.http.HttpClientProvider
import com.avito.http.RetryInterceptor
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.internal.JsonRpcRequestProvider
import com.avito.report.model.EntryTypeAdapterFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.util.concurrent.TimeUnit

object ReportsApiFactory {

    private const val TIMEOUT_SEC = 10L

    /**
     * for tests
     */
    internal val gson: Gson = GsonBuilder()
        .registerTypeAdapterFactory(EntryTypeAdapterFactory())
        .create()

    fun create(
        host: String,
        httpClientProvider: HttpClientProvider,
        loggerFactory: LoggerFactory,
        retryRequests: Boolean = true
    ): ReportsApi {
        return ReportsApiImpl(
            loggerFactory = loggerFactory,
            requestProvider = JsonRpcRequestProvider(
                host = host,
                httpClient = httpClientProvider.provide()
                    .connectTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
                    .writeTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
                    .apply {
                        if (retryRequests) {
                            addInterceptor(
                                RetryInterceptor(
                                    retries = 3,
                                    allowedMethods = listOf("POST"),
                                    logger = loggerFactory.create<ReportsApi>()
                                )
                            )
                        }
                    }
                    .build(),
                gson = gson
            )
        )
    }
}
