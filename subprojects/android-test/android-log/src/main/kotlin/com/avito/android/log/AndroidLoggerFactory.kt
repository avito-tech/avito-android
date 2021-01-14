package com.avito.android.log

import com.avito.android.elastic.ElasticConfig
import com.avito.android.log.destination.AndroidLogDestination
import com.avito.android.sentry.SentryConfig
import com.avito.logger.DefaultLogger
import com.avito.logger.Logger
import com.avito.logger.LoggerFactory
import com.avito.logger.handler.CombinedHandler
import com.avito.logger.handler.DefaultLoggingHandler
import com.avito.logger.handler.LoggingHandler

class AndroidLoggerFactory(
    private val elasticConfig: ElasticConfig,
    private val sentryConfig: SentryConfig
) : LoggerFactory {

    override fun create(tag: String): Logger {

        val metadata = AndroidMetadata(tag)

        val defaultHandler = defaultHandler(metadata)

        return DefaultLogger(
            debugHandler = defaultHandler,
            infoHandler = defaultHandler,
            warningHandler = defaultHandler,
            criticalHandler = criticalHandler(defaultHandler, metadata)
        )
    }

    private fun defaultHandler(metadata: AndroidMetadata): LoggingHandler {
        return if (elasticConfig is ElasticConfig.Enabled) {
            CombinedHandler(
                listOf(
                    androidLogHandler(metadata),
                    elasticHandler(metadata)
                )
            )
        } else {
            androidLogHandler(metadata)
        }
    }

    private fun androidLogHandler(metadata: AndroidMetadata): LoggingHandler {
        return DefaultLoggingHandler(destination = AndroidLogDestination(metadata))
    }

    private fun elasticHandler(metadata: AndroidMetadata): LoggingHandler {
        return DefaultLoggingHandler(destination = ElasticDestinationFactory.create(elasticConfig, metadata))
    }

    private fun criticalHandler(defaultHandler: LoggingHandler, metadata: AndroidMetadata): LoggingHandler {
        return if (sentryConfig is SentryConfig.Enabled) {
            val sentryHandler =
                DefaultLoggingHandler(
                    destination = SentryDestinationFactory.create(
                        config = sentryConfig,
                        metadata = metadata
                    )
                )
            CombinedHandler(
                listOf(
                    defaultHandler,
                    sentryHandler
                )
            )
        } else {
            defaultHandler
        }
    }
}
