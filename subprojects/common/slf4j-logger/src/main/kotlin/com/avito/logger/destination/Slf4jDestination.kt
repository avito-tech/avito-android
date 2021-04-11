package com.avito.logger.destination

import com.avito.logger.LogLevel
import com.avito.logger.LoggingDestination
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Slf4jDestination(
    private val tag: String,
    private val verboseMode: LogLevel
) : LoggingDestination {

    @Transient
    private lateinit var _logger: Logger

    private fun logger(): Logger {
        if (!::_logger.isInitialized) {
            _logger = LoggerFactory.getLogger(tag)
        }
        return _logger
    }

    override fun write(level: LogLevel, message: String, throwable: Throwable?) {
        with(logger()) {
            when (level) {
                LogLevel.DEBUG -> if (verboseMode == LogLevel.DEBUG) {
                    error(message, throwable)
                } else {
                    debug(message, throwable)
                }
                LogLevel.INFO -> if (verboseMode == LogLevel.DEBUG || verboseMode == LogLevel.INFO) {
                    error(message, throwable)
                } else {
                    info(message, throwable)
                }
                LogLevel.WARNING -> if (verboseMode == LogLevel.DEBUG
                    || verboseMode == LogLevel.INFO
                    || verboseMode == LogLevel.WARNING
                ) {
                    error(message, throwable)
                } else {
                    warn(message, throwable)
                }
                LogLevel.CRITICAL -> error(message, throwable)
            }
        }
    }
}
