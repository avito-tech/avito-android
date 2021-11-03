package com.avito.logger.handler

import com.avito.logger.LogLevel
import com.avito.logger.LoggerMetadata

public class PrintlnLoggingHandlerProvider(
    private val acceptedLogLevel: LogLevel,
    private val printStackTrace: Boolean
) : LoggingHandlerProvider {

    override fun provide(metadata: LoggerMetadata): LoggingHandler {
        return PrintlnLoggingHandler(acceptedLogLevel, printStackTrace)
    }
}

internal class PrintlnLoggingHandler(
    acceptedLogLevel: LogLevel,
    private val printStackTrace: Boolean
) : LogLevelLoggingHandler(acceptedLogLevel) {

    override fun handleIfAcceptLogLevel(level: LogLevel, message: String, error: Throwable?) {
        println(message)
        if (printStackTrace && error != null) {
            error.printStackTrace()
        }
    }
}
