package com.avito.filestorage

import com.avito.http.HttpClientProvider
import com.avito.logger.LoggerFactory
import com.avito.time.TimeProvider
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.util.concurrent.TimeUnit

object RemoteStorageFactory {

    fun create(
        endpoint: String,
        httpClientProvider: HttpClientProvider,
        loggerFactory: LoggerFactory,
        timeProvider: TimeProvider,
    ): RemoteStorage = HttpRemoteStorage(
        endpoint = requireNotNull(endpoint.toHttpUrlOrNull()) { "Can't parse endpoint: $endpoint" },
        httpClient = httpClientProvider.provide()
            .connectTimeout(10L, TimeUnit.SECONDS)
            .writeTimeout(30L, TimeUnit.SECONDS)
            .readTimeout(10L, TimeUnit.SECONDS)
            .build(),
        loggerFactory = loggerFactory,
        timeProvider = timeProvider
    )
}
