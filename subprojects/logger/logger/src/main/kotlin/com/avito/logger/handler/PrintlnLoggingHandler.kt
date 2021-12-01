package com.avito.logger.handler

import com.avito.logger.LogLevel
import com.avito.logger.metadata.LoggerMetadata

public class PrintlnLoggingHandlerProvider(
    private val acceptedLogLevel: LogLevel,
    private val printStackTrace: Boolean
) : LoggingHandlerProvider {

    override fun provide(metadata: LoggerMetadata): LoggingHandler {
        return PrintlnLoggingHandler(acceptedLogLevel, printStackTrace, metadata.asMessagePrefix)
    }
}

internal class PrintlnLoggingHandler(
    acceptedLogLevel: LogLevel,
    private val printStackTrace: Boolean,
    private val messagePrefix: String
) : LogLevelLoggingHandler(acceptedLogLevel) {

    override fun handleIfAcceptLogLevel(level: LogLevel, message: String, error: Throwable?) {
        println("$messagePrefix $message")
        if (printStackTrace && error != null) {
            error.printStackTrace()
        }
    }
}
