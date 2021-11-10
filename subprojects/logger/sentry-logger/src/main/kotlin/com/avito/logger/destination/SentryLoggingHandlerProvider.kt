package com.avito.logger.destination

import com.avito.logger.LogLevel
import com.avito.logger.LoggerMetadata
import com.avito.logger.handler.LoggingHandler
import com.avito.logger.handler.LoggingHandlerProvider
import io.sentry.SentryClient

public class SentryLoggingHandlerProvider(
    private val acceptedLogLevel: LogLevel,
    private val sentryClient: SentryClient,
) : LoggingHandlerProvider {
    override fun provide(metadata: LoggerMetadata): LoggingHandler {
        return SentryLoggingHandler(acceptedLogLevel, sentryClient, metadata.asMap())
    }
}
