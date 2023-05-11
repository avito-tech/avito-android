package com.avito.logger.destination

import com.avito.logger.LogLevel
import com.avito.logger.handler.LogLevelLoggingHandler

@Deprecated("Use only after support actual sentry-java version")
internal class SentryLoggingHandler(
    acceptedLogLevel: LogLevel,
) : LogLevelLoggingHandler(acceptedLogLevel) {

    override fun handleIfAcceptLogLevel(level: LogLevel, message: String, error: Throwable?) {
        // unsupported
        // `libs.sentry` is too far from actual sentry-java. It creates version conflicts
    }
}
