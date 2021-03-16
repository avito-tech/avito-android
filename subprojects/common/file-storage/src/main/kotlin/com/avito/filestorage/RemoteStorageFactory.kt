package com.avito.filestorage

import com.avito.http.HttpLogger
import com.avito.http.RetryInterceptor
import com.avito.logger.Logger
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.time.TimeProvider
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object RemoteStorageFactory {

    private const val TIMEOUT_SEC = 120L

    fun create(
        endpoint: String,
        loggerFactory: LoggerFactory,
        timeProvider: TimeProvider,
        httpClient: OkHttpClient = getHttpClient(
            logger = loggerFactory.create<RemoteStorage>(),
            retryInterceptor = null,
            verbose = false // do not enable for production, generates a ton of logs
        )
    ): RemoteStorage = HttpRemoteStorage(
        endpoint = requireNotNull(endpoint.toHttpUrlOrNull()) { "Can't parse endpoint: $endpoint" },
        httpClient = httpClient,
        loggerFactory = loggerFactory,
        timeProvider = timeProvider
    )

    private fun getHttpClient(
        verbose: Boolean,
        logger: Logger,
        retryInterceptor: RetryInterceptor?
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
            .connectTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
            .apply {
                if (verbose) {
                    addInterceptor(
                        HttpLoggingInterceptor(HttpLogger(logger))
                            .setLevel(HttpLoggingInterceptor.Level.BODY)
                    )
                }
                if (retryInterceptor != null) {
                    addInterceptor(retryInterceptor)
                }
            }
            .build()
    }
}
