package com.avito.logger.handler

import com.avito.logger.LogLevel
import com.avito.logger.LoggingDestination
import com.avito.logger.LoggingFormatter

class DefaultLoggingHandler(
    private val formatter: LoggingFormatter? = null,
    private val destination: LoggingDestination
) : LoggingHandler {

    override fun write(level: LogLevel, message: String, error: Throwable?) {
        destination.write(
            level,
            formatter?.format(message) ?: message,
            error
        )
    }
}
