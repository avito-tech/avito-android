package com.avito.android.tech_budget.internal.warnings.log

import org.gradle.api.logging.LogLevel
import org.gradle.internal.logging.events.LogEvent
import org.gradle.internal.logging.events.OutputEvent
import org.gradle.internal.logging.events.OutputEventListener

internal class TaskLogsDumper(
    private val targetLogLevel: LogLevel,
    private val logWriter: LogWriter,
) : OutputEventListener {

    override fun onOutput(event: OutputEvent) {
        if (event is LogEvent && event.logLevel == targetLogLevel) {
            logWriter.save(event.message)
        }
    }
}
