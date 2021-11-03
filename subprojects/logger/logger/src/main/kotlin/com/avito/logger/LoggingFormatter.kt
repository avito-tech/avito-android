package com.avito.logger

import com.avito.logger.handler.LoggingHandler

public interface LoggingFormatter {

    public fun format(metadata: LoggerMetadata, message: String): String

    public object NoOpFormatter : LoggingFormatter {
        override fun format(metadata: LoggerMetadata, message: String): String {
            // do nothing
            return message
        }
    }
}

internal class FormatterLoggingHandler(
    private val formatter: LoggingFormatter,
    private val delegate: LoggingHandler,
    private val metadata: LoggerMetadata
) : LoggingHandler {

    override fun write(level: LogLevel, message: String, error: Throwable?) {
        delegate.write(
            level,
            formatter.format(metadata, message),
            error
        )
    }
}
