package com.avito.utils.logging

import com.avito.android.sentry.SentryConfig
import com.avito.android.sentry.sentryClient
import io.sentry.SentryClient
import io.sentry.event.Event
import io.sentry.event.EventBuilder
import io.sentry.event.interfaces.ExceptionInterface

/**
 * @param config can't pass SentryClient directly here, even if it's already created,
 *               because CILogger instance should be serializable
 */
internal class SentryDestination(private val config: SentryConfig) : CILoggingDestination {

    @Transient
    private lateinit var _sentry: SentryClient

    private fun sentry(): SentryClient {
        if (!::_sentry.isInitialized) {
            _sentry = sentryClient(config)
        }
        return _sentry
    }

    override fun write(message: String, throwable: Throwable?) {
        throwable?.also {
            val eventBuilder =
                EventBuilder().withMessage(message)
                    .withLevel(Event.Level.ERROR)
                    .withSentryInterface(ExceptionInterface(throwable))
            sentry().sendEvent(eventBuilder)
        }
    }

    override fun child(tag: String): CILoggingDestination = this
}
