package com.avito.android.build_verdict.internal

import org.gradle.api.logging.LogLevel
import org.gradle.internal.logging.events.LogEvent
import org.gradle.internal.logging.events.OutputEvent
import org.gradle.internal.logging.events.OutputEventListener
import org.gradle.internal.logging.events.ProgressCompleteEvent
import org.gradle.internal.logging.events.RenderableOutputEvent
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

    private val kspErrorLines = mutableMapOf<OperationIdentifier, KspErrorLines>()

    override fun onOutput(event: OutputEvent) {
        if (event is ProgressCompleteEvent) {
            kspErrorLines.remove(event.progressOperationId)
            return
        }
        if (event !is RenderableOutputEvent) return
        val id = event.buildOperationId ?: return
        val listener = listeners[id] ?: return
        val message = when (event) {
            is LogEvent -> event.message
            is StyledTextOutputEvent -> event.spans.joinToString(separator = "") { span -> span.text }
            else -> return
        }
        val isErrorEvent = event.logLevel == acceptedLevel && event.category in acceptedCategories
        val isAccepted = isErrorEvent ||
            event.category == STDOUT_CATEGORY && kspErrorLines.getOrPut(id) { KspErrorLines() }.accept(message)
        if (isAccepted) {
            listener.onLogMessage(message)
        }
    }

    private companion object {
        const val STDOUT_CATEGORY = "system.out"
    }
}
