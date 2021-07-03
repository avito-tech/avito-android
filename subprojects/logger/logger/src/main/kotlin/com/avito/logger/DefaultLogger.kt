package com.avito.logger

import com.avito.logger.handler.LoggingHandler
import java.io.Serializable

public class DefaultLogger(
    private val debugHandler: LoggingHandler,
    private val infoHandler: LoggingHandler,
    private val warningHandler: LoggingHandler,
    private val criticalHandler: LoggingHandler
) : Logger, Serializable {

    override fun debug(msg: String) {
        debugHandler.write(LogLevel.DEBUG, msg)
    }

    override fun info(msg: String) {
        infoHandler.write(LogLevel.INFO, msg)
    }

    override fun warn(msg: String, error: Throwable?) {
        warningHandler.write(LogLevel.WARNING, msg, error)
    }

    override fun critical(msg: String, error: Throwable) {
        criticalHandler.write(LogLevel.CRITICAL, msg, error)
    }
}
