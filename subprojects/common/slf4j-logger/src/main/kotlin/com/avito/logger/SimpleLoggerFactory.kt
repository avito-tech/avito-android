package com.avito.logger

import com.avito.logger.destination.Slf4jDestination
import com.avito.logger.formatter.AppendClassNameFormatter
import com.avito.logger.handler.DefaultLoggingHandler

class SimpleLoggerFactory : LoggerFactory {

    override fun create(tag: String): Logger {

        val defaultHandler = DefaultLoggingHandler(
            destination = Slf4jDestination(tag, verboseMode = LogLevel.CRITICAL),
            formatter = AppendClassNameFormatter(tag)
        )

        return DefaultLogger(
            debugHandler = defaultHandler,
            infoHandler = defaultHandler,
            warningHandler = defaultHandler,
            criticalHandler = defaultHandler
        )
    }
}
