package com.avito.android.elastic

import com.avito.logger.LoggerFactory
import com.avito.logger.SimpleLoggerFactory
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider

public object ElasticClientFactory {

    private val loggerFactory: LoggerFactory by lazy { SimpleLoggerFactory() }

    private val timeProvider: TimeProvider by lazy { DefaultTimeProvider(loggerFactory) }

    private val cache = mutableMapOf<ElasticConfig, HttpElasticClient>()

    public fun provide(config: ElasticConfig): ElasticClient = when (config) {

        is ElasticConfig.Disabled -> StubElasticClient

        is ElasticConfig.Enabled -> cache.getOrPut(
            key = config,
            defaultValue = {
                HttpElasticClient(
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
