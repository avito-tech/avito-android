package com.avito.report

import com.avito.http.HttpClientProvider
import com.avito.http.RetryPolicy
import com.avito.logger.LoggerFactory
import com.avito.report.internal.JsonRpcRequestProvider
import com.avito.report.model.EntryTypeAdapterFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Request
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
        httpClientProvider: HttpClientProvider,
        loggerFactory: LoggerFactory,
        retryPolicy: RetryPolicy? = RetryPolicy(allowedMethods = listOf("POST"))
    ): ReportsApi {
        return ReportsApiImpl(
            loggerFactory = loggerFactory,
            requestProvider = JsonRpcRequestProvider(
                host = host,
                httpClient = httpClientProvider.provide(
                    serviceName = "reports",
                    timeoutMs = TimeUnit.SECONDS.toMillis(TIMEOUT_SEC),
                    retryPolicy = retryPolicy
                ),
                gson = gson
            )
        )
    }

    // for tests
    internal fun Request.describeJsonRpc(): String = "${url.redact()} method: ${tag()}"
}
