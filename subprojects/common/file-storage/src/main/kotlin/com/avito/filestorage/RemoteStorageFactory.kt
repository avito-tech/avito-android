package com.avito.filestorage

import com.avito.http.HttpClientProvider
import com.avito.http.RequestMetadataInterceptor
import com.avito.http.internal.RequestMetadata
import com.avito.logger.LoggerFactory
import com.avito.time.TimeProvider
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request
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
            timeoutMs = TimeUnit.SECONDS.toMillis(TIMEOUT_SEC),
            retryPolicy = null,
            metadataInterceptor = RequestMetadataInterceptor { request ->
                RequestMetadata(
                    serviceName = "file-storage",
                    methodName = getMethodInfo(request)
                )
            }
        ).build(),
        loggerFactory = loggerFactory,
        timeProvider = timeProvider
    )

    private fun getMethodInfo(request: Request): String {
        val path = request.url.encodedPath.trimStart('/').replace('/', '_')
        val fileType = request.header("X-Extension") ?: "txt"
        return "${path}_$fileType"
    }
}
