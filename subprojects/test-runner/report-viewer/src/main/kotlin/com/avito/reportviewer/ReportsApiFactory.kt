package com.avito.reportviewer

import com.avito.http.HttpClientProvider
import com.avito.http.RetryInterceptor
import com.avito.jsonrpc.JsonRpcClient
import com.avito.report.serialize.createReportGson
import com.avito.reportviewer.internal.ReportsApiImpl
import java.util.concurrent.TimeUnit

public object ReportsApiFactory {

    private const val DEFAULT_TIMEOUT_SEC = 10L

    public fun create(
        host: String,
        httpClientProvider: HttpClientProvider,
        retryRequests: Boolean = true,
        readWriteTimeoutSec: Long = DEFAULT_TIMEOUT_SEC
    ): ReportsApi {
        return ReportsApiImpl(
            client = JsonRpcClient(
                host = host,
                httpClient = httpClientProvider.provide()
                    .writeTimeout(readWriteTimeoutSec, TimeUnit.SECONDS)
                    .readTimeout(readWriteTimeoutSec, TimeUnit.SECONDS)
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
