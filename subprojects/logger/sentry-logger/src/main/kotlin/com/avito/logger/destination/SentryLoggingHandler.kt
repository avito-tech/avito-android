package com.avito.logger.destination

import com.avito.logger.LogLevel
import com.avito.logger.handler.LogLevelLoggingHandler
import io.sentry.SentryClient
import io.sentry.event.Event
import io.sentry.event.EventBuilder
import io.sentry.event.interfaces.ExceptionInterface

internal class SentryLoggingHandler(
    acceptedLogLevel: LogLevel,
    private val sentryClient: SentryClient,
    private val metadata: Map<String, String>
) : LogLevelLoggingHandler(acceptedLogLevel) {

    override fun handleIfAcceptLogLevel(level: LogLevel, message: String, error: Throwable?) {
        val eventBuilder = EventBuilder()
            .withMessage(message)
            .withLevel(level.toSentryLevel())
            .appendMetadata()

        if (error != null) {
            eventBuilder.withSentryInterface(ExceptionInterface(error))
        }

        sentryClient.sendEvent(eventBuilder)
    }

    private fun LogLevel.toSentryLevel(): Event.Level {
        return when (this) {
            LogLevel.DEBUG -> Event.Level.DEBUG
            LogLevel.INFO -> Event.Level.INFO
            LogLevel.WARNING -> Event.Level.WARNING
            LogLevel.CRITICAL -> Event.Level.FATAL
        }
    }

    private fun EventBuilder.appendMetadata(): EventBuilder = apply {
        metadata
            .filterValues { it.isNotBlank() }
            .forEach { (key, value) ->
                withTag(key, value)
            }
    }
}
