package com.avito.report

import com.avito.http.HttpClientProvider
import com.avito.http.RetryInterceptor
import com.avito.logger.LoggerFactory
import com.avito.report.internal.JsonRpcClient
import com.avito.report.internal.ReportsApiImpl
import com.avito.report.serialize.createReportGson
import java.util.concurrent.TimeUnit

public object ReportsApiFactory {

    private const val TIMEOUT_SEC = 10L

    public fun create(
        host: String,
        httpClientProvider: HttpClientProvider,
        loggerFactory: LoggerFactory,
        retryRequests: Boolean = true
    ): ReportsApi {
        return ReportsApiImpl(
            loggerFactory = loggerFactory,
            client = JsonRpcClient(
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
                                    allowedMethods = listOf("POST")
                                )
                            )
                        }
                    }
                    .build(),
                gson = createReportGson()
            )
        )
    }
}
