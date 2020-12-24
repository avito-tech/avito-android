package com.avito.android.log

import com.avito.android.log.destination.AndroidLogDestination
import com.avito.android.log.destination.SentryDestination
import com.avito.android.sentry.SentryConfig
import com.avito.logger.DefaultLogger
import com.avito.logger.Logger
import com.avito.logger.LoggerFactory
import com.avito.logger.handler.CombinedHandler
import com.avito.logger.handler.DefaultLoggingHandler
import com.avito.logger.handler.LoggingHandler

class AndroidLoggerFactory(private val sentryConfig: SentryConfig) : LoggerFactory {

    override fun create(tag: String): Logger {

        val defaultHandler = defaultHandler(tag)

        return DefaultLogger(
            debugHandler = defaultHandler,
            infoHandler = defaultHandler,
            warningHandler = defaultHandler,
            criticalHandler = criticalHandler(defaultHandler)
        )
    }

    private fun defaultHandler(tag: String): LoggingHandler {
        return DefaultLoggingHandler(destination = AndroidLogDestination(tag))
    }

    private fun criticalHandler(defaultHandler: LoggingHandler): LoggingHandler {
        return if (sentryConfig is SentryConfig.Enabled) {
            val sentryHandler = DefaultLoggingHandler(destination = SentryDestination(sentryConfig))
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
