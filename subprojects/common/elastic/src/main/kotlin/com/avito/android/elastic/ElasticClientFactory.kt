package com.avito.android.elastic

import com.avito.logger.LoggerFactory
import com.avito.logger.SimpleLoggerFactory
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider

public object ElasticClientFactory {

    private val loggerFactory: LoggerFactory = SimpleLoggerFactory()

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
                    loggerFactory = loggerFactory,
                    dateFormatChecker = if (config.checkDateFormatter) {
                        ElasticDateFormatChecker()
                    } else {
                        StubDateFormatChecker
                    }
                )
            }
        )
    }
}
