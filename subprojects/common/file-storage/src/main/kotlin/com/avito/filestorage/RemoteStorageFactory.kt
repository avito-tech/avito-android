package com.avito.filestorage

import com.avito.http.HttpClientProvider
import com.avito.logger.LoggerFactory
import com.avito.time.TimeProvider
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.util.concurrent.TimeUnit

object RemoteStorageFactory {

    private const val TIMEOUT_SEC = 30L
    private const val CONNECTION_TIMEOUT_SEC = 10L

    fun create(
        endpoint: String,
        httpClientProvider: HttpClientProvider,
        loggerFactory: LoggerFactory,
        timeProvider: TimeProvider,
    ): RemoteStorage {
        return HttpRemoteStorage(
            endpoint = requireNotNull(endpoint.toHttpUrlOrNull()) { "Can't parse endpoint: $endpoint" },
            httpClient = httpClientProvider.provide()
                .connectTimeout(CONNECTION_TIMEOUT_SEC, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
                .build(),
            loggerFactory = loggerFactory,
            timeProvider = timeProvider
        )
    }
}
