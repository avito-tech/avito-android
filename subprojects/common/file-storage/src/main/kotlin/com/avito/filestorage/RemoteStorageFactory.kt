package com.avito.filestorage

import com.avito.http.HttpClientProvider
import com.avito.logger.LoggerFactory
import com.avito.time.TimeProvider
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.util.concurrent.TimeUnit

object RemoteStorageFactory {

    private const val TIMEOUT_SEC = 120L

    fun create(
        endpoint: String,
        httpClientProvider: HttpClientProvider,
        loggerFactory: LoggerFactory,
        timeProvider: TimeProvider
    ): RemoteStorage = HttpRemoteStorage(
        endpoint = requireNotNull(endpoint.toHttpUrlOrNull()) { "Can't parse endpoint: $endpoint" },
        httpClient = httpClientProvider.provide(
            serviceName = "file-storage",
            timeoutMs = TimeUnit.SECONDS.toMillis(TIMEOUT_SEC),
            retryPolicy = null
        ),
        loggerFactory = loggerFactory,
        timeProvider = timeProvider
    )
}
