package com.avito.logger.destination

import com.avito.logger.LogLevel
import com.avito.logger.LoggingDestination
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Slf4jDestination(private val tag: String) : LoggingDestination {

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
                LogLevel.DEBUG -> debug(message, throwable)
                LogLevel.INFO -> info(message, throwable)
                LogLevel.WARNING -> warn(message, throwable)
                LogLevel.CRITICAL -> error(message, throwable)
            }
        }
    }
}
