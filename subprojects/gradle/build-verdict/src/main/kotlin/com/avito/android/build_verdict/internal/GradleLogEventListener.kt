package com.avito.android.build_verdict.internal

import org.gradle.api.logging.LogLevel
import org.gradle.internal.logging.events.CategorisedOutputEvent
import org.gradle.internal.logging.events.LogEvent
import org.gradle.internal.logging.events.OutputEvent
import org.gradle.internal.logging.events.OutputEventListener
import org.gradle.internal.logging.events.StyledTextOutputEvent
import org.gradle.internal.operations.OperationIdentifier

internal class GradleLogEventListener(
    private val acceptedLevel: LogLevel = LogLevel.ERROR,
    /**
     * "org.gradle.api.Task" - org.gradle.api.internal.AbstractTask.BUILD_LOGGER
     * "system.err" - org.gradle.internal.logging.source.DefaultStdErrLoggingSystem
     */
    private val acceptedCategories: List<String> = listOf("org.gradle.api.Task", "system.err"),
    private val listeners: Map<OperationIdentifier, LogMessageListener>
) : OutputEventListener {

    override fun onOutput(event: OutputEvent) {
        when (event) {
            is CategorisedOutputEvent -> {
                val isEventAccepted = event.logLevel == acceptedLevel && event.category in acceptedCategories
                if (isEventAccepted) {
                    when (event) {
                        is LogEvent -> onLogEvent(event)
                        is StyledTextOutputEvent -> onStyledEvent(event)
                    }
                }
            }
        }
    }

    private fun onStyledEvent(event: StyledTextOutputEvent) {
        val id = event.buildOperationId
        if (id != null) {
            listeners[id]?.let { listener ->
                listener.onLogMessage(event.spans.joinToString(separator = "") { span -> span.text })
            }
        }
    }

    private fun onLogEvent(event: LogEvent) {
        val id = event.buildOperationId
        if (id != null) {
            listeners[id]?.onLogMessage(event.message)
        }
    }
}
