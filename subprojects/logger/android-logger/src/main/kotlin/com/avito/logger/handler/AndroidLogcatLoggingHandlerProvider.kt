package com.avito.logger.handler

import com.avito.logger.LogLevel
import com.avito.logger.metadata.LoggerMetadata

public class AndroidLogcatLoggingHandlerProvider(private val acceptedLogLevel: LogLevel) : LoggingHandlerProvider {

    override fun provide(metadata: LoggerMetadata): LoggingHandler {
        return AndroidLogcatLoggingHandler(
            tag = metadata.asMessagePrefix,
            acceptedLogLevel = acceptedLogLevel
        )
    }
}
