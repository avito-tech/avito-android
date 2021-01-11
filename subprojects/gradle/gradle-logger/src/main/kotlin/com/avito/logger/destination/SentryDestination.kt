package com.avito.logger.destination

import com.avito.android.sentry.SentryConfig
import com.avito.android.sentry.sentryClient
import com.avito.logger.LogLevel
import com.avito.logger.LoggerMetadata
import com.avito.logger.LoggingDestination
import io.sentry.SentryClient
import io.sentry.event.Event
import io.sentry.event.EventBuilder
import io.sentry.event.interfaces.ExceptionInterface

/**
 * @param config can't pass SentryClient directly here, even if it's already created,
 *               because logger instance should be serializable
 */
internal class SentryDestination(
    private val config: SentryConfig,
    private val metadata: LoggerMetadata
) : LoggingDestination {

    @Transient
    private lateinit var _sentry: SentryClient

    private fun sentry(): SentryClient {
        if (!::_sentry.isInitialized) {
            _sentry = sentryClient(config)
        }
        return _sentry
    }

    override fun write(level: LogLevel, message: String, throwable: Throwable?) {
        throwable?.also {
            val eventBuilder = EventBuilder().withMessage(message)
                .withLevel(level.toSentryLevel())
                .withSentryInterface(ExceptionInterface(throwable))
                .appendMetadata()

            sentry().sendEvent(eventBuilder)
        }
    }

    private fun LogLevel.toSentryLevel(): Event.Level {
        return when (this) {
            LogLevel.DEBUG -> Event.Level.DEBUG
            LogLevel.INFO -> Event.Level.INFO
            LogLevel.WARNING -> Event.Level.WARNING
            LogLevel.CRITICAL -> Event.Level.ERROR
        }
    }

    private fun EventBuilder.appendMetadata(): EventBuilder = apply {
        withTag("tag", metadata.tag)

        if (!metadata.pluginName.isNullOrBlank()) {
            withTag("plugin_name", metadata.pluginName)
        }

        if (!metadata.projectPath.isNullOrBlank()) {
            withTag("project_path", metadata.projectPath)
        }

        if (!metadata.taskName.isNullOrBlank()) {
            withTag("task_name", metadata.taskName)
        }
    }
}
