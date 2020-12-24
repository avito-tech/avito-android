package com.avito.android.log.destination

import com.avito.android.sentry.SentryConfig
import com.avito.android.sentry.sentryClient
import com.avito.logger.LogLevel
import com.avito.logger.LoggingDestination
import io.sentry.SentryClient

class SentryDestination(private val config: SentryConfig.Enabled) : LoggingDestination {

    @Transient
    private lateinit var _sentry: SentryClient

    private fun sentry(): SentryClient {
        if (!::_sentry.isInitialized) {
            _sentry = sentryClient(config)
        }
        return _sentry
    }

    override fun write(level: LogLevel, message: String, throwable: Throwable?) {
        throwable?.let {
            sentry().sendException(throwable)
        }
    }
}
