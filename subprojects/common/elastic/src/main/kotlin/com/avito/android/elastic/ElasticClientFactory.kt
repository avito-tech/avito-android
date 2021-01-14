package com.avito.android.elastic

import com.avito.logger.LoggerFactory
import com.avito.logger.SimpleLoggerFactory
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object ElasticClientFactory {

    private const val defaultTimeoutSec = 10L

    private val loggerFactory: LoggerFactory by lazy { SimpleLoggerFactory() }

    private val timeProvider: TimeProvider by lazy { DefaultTimeProvider(loggerFactory) }

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(defaultTimeoutSec, TimeUnit.SECONDS)
            .readTimeout(defaultTimeoutSec, TimeUnit.SECONDS)
            .build()
    }

    private val cache = mutableMapOf<ElasticConfig, HttpElasticClient>()

    fun provide(config: ElasticConfig): ElasticClient {
        return when (config) {
            is ElasticConfig.Disabled -> StubElasticClient
            is ElasticConfig.Enabled -> cache.getOrPut(
                key = config,
                defaultValue = {
                    HttpElasticClient(
                        okHttpClient = httpClient,
                        timeProvider = timeProvider,
                        endpoints = config.endpoints,
                        indexPattern = config.indexPattern,
                        buildId = config.buildId,
                        loggerFactory = loggerFactory
                    )
                }
            )
        }
    }
}
