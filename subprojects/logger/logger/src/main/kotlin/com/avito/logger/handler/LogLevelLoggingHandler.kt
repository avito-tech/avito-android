package com.avito.logger.handler

import com.avito.logger.LogLevel

public abstract class LogLevelLoggingHandler(
    private val acceptedLogLevel: LogLevel
) : LoggingHandler {

    public final override fun write(
        level: LogLevel,
        message: String,
        error: Throwable?
    ) {
        if (acceptedLogLevel <= level) {
            handleIfAcceptLogLevel(level, message, error)
        }
    }

    protected abstract fun handleIfAcceptLogLevel(
        level: LogLevel,
        message: String,
        error: Throwable?
    )
}
