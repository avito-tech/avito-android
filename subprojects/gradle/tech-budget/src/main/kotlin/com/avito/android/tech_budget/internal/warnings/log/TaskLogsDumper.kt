package com.avito.android.tech_budget.internal.warnings.log

import com.avito.android.tech_budget.internal.warnings.task.TaskBuildOperationIdProvider
import org.gradle.api.logging.LogLevel
import org.gradle.internal.logging.events.LogEvent
import org.gradle.internal.logging.events.OutputEvent
import org.gradle.internal.logging.events.OutputEventListener
import org.gradle.util.Path

internal class TaskLogsDumper(
    private val targetLogLevel: LogLevel,
    private val logWriter: LogWriter,
    private val taskPath: Path,
    private val taskBuildOperationIdProvider: TaskBuildOperationIdProvider
) : OutputEventListener {

    override fun onOutput(event: OutputEvent) {
        val taskBuildOperationId = taskBuildOperationIdProvider.getBuildOperationId(taskPath)
        if (event is LogEvent && event.logLevel == targetLogLevel && event.buildOperationId == taskBuildOperationId) {
            logWriter.save(event.message)
        }
    }
}
