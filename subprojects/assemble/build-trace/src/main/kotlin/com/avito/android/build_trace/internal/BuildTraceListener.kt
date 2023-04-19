package com.avito.android.build_trace.internal

import com.avito.android.critical_path.CriticalPathListener
import com.avito.android.critical_path.TaskOperation
import com.avito.android.gradle.metric.AbstractBuildEventsListener
import com.avito.android.gradle.profile.BuildProfile
import com.avito.android.gradle.profile.TaskExecution
import com.avito.android.trace.TraceEvent
import com.avito.android.trace.TraceReport
import com.avito.android.trace.TraceReportFileAdapter
import com.avito.graph.OperationsPath
import com.avito.logger.LoggerFactory
import org.gradle.BuildResult
import org.gradle.api.Task
import java.io.File
import java.time.Instant
import java.util.Collections

internal class BuildTraceListener(
    private val output: File,
    loggerFactory: LoggerFactory,
) : AbstractBuildEventsListener(), CriticalPathListener {

    override val name: String = "BuildTrace"
    private val logger = loggerFactory.create(name)

    private val eventProvider = TraceEventProvider()
    private val events: MutableList<TraceEvent> = Collections.synchronizedList(mutableListOf())

    override fun afterExecute(task: Task, state: TaskExecution) {
        events.add(eventProvider.taskExecutionEvent(task, state))
    }

    override fun buildFinished(buildResult: BuildResult, profile: BuildProfile) {
        events.add(eventProvider.initWithConfigurationEvent(profile))
        events.add(eventProvider.executionStartEvent(profile))
        events.add(eventProvider.executionFinishEvent(profile))
    }

    override fun onCriticalPathReady(path: OperationsPath<TaskOperation>) {
        logger.info("Start onCriticalPathReady ${Instant.now()}")
        writeReport(path)
        logger.info("End onCriticalPathReady ${Instant.now()}")
    }

    private fun writeReport(criticalPath: OperationsPath<TaskOperation>) {
        output.parentFile.mkdirs()

        val report = TraceReport(
            traceEvents = enrichCriticalPath(events, criticalPath)
        )
        TraceReportFileAdapter(output).write(report)
        logger.info("Build trace: ${output.path}")
    }

    private fun enrichCriticalPath(
        traceEvents: List<TraceEvent>,
        criticalPath: OperationsPath<TaskOperation>
    ): List<TraceEvent> {
        return traceEvents
            .map { event ->
                eventProvider.criticalPathEvent(event, criticalPath)
            }
    }
}
