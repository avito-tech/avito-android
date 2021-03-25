package com.avito.filestorage

import com.avito.http.HttpClientProvider
import com.avito.logger.LoggerFactory
import com.avito.time.TimeProvider
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import java.util.concurrent.TimeUnit

object RemoteStorageFactory {

    private const val TIMEOUT_SEC = 120L

    fun create(
        endpoint: String,
        httpClientProvider: HttpClientProvider,
        loggerFactory: LoggerFactory,
        timeProvider: TimeProvider,
        testInterceptor: Interceptor? = null,
    ): RemoteStorage = HttpRemoteStorage(
        endpoint = requireNotNull(endpoint.toHttpUrlOrNull()) { "Can't parse endpoint: $endpoint" },
        httpClient = httpClientProvider.provide()
            .apply { if (testInterceptor != null) addInterceptor(testInterceptor) }
            .connectTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
            .build(),
        loggerFactory = loggerFactory,
        timeProvider = timeProvider
    )
}
