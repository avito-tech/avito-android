package com.avito.android.elastic

import com.avito.logger.LogLevel
import com.avito.logger.LoggerFactory
import com.avito.logger.LoggerFactoryBuilder
import com.avito.logger.handler.PrintlnLoggingHandlerProvider
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider

public object ElasticClientFactory {

    private val loggerFactory: LoggerFactory = LoggerFactoryBuilder()
        .addLoggingHandlerProvider(PrintlnLoggingHandlerProvider(LogLevel.DEBUG, false))
        .build()

    private val timeProvider: TimeProvider = DefaultTimeProvider()

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
