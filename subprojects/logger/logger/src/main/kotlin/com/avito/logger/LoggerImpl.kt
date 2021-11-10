package com.avito.logger

import com.avito.logger.handler.LoggingHandler

internal class LoggerImpl(
    private val handler: LoggingHandler,
) : Logger {

    override fun debug(msg: String) {
        handler.write(LogLevel.DEBUG, msg, null)
    }

    override fun info(msg: String) {
        handler.write(LogLevel.INFO, msg, null)
    }

    override fun warn(msg: String, error: Throwable?) {
        handler.write(LogLevel.WARNING, msg, error)
    }

    override fun critical(msg: String, error: Throwable) {
        handler.write(LogLevel.CRITICAL, msg, error)
    }
}
