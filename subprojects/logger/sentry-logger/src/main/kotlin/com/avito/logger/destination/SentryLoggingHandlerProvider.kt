package com.avito.logger.destination

import com.avito.logger.LogLevel
import com.avito.logger.handler.LoggingHandler
import com.avito.logger.handler.LoggingHandlerProvider
import com.avito.logger.metadata.LoggerMetadata

@Suppress("DEPRECATION")
public class SentryLoggingHandlerProvider(
    private val acceptedLogLevel: LogLevel,
) : LoggingHandlerProvider {
    override fun provide(metadata: LoggerMetadata): LoggingHandler {
        return SentryLoggingHandler(acceptedLogLevel)
    }
}
