package com.avito.android.build_trace.internal

import com.avito.android.critical_path.CriticalPathListener
import com.avito.android.critical_path.TaskOperation
import com.avito.android.gradle.metric.AbstractBuildEventsListener
import com.avito.android.gradle.profile.BuildProfile
import com.avito.android.gradle.profile.TaskExecution
import com.avito.android.trace.TraceEvent
import com.avito.android.trace.TraceReport
import com.avito.android.trace.TraceReport.Companion.BUILD_FINISHED_REPORT_SOURCE
import com.avito.android.trace.TraceReport.Companion.REPORT_SOURCE_METADATA_KEY
import com.avito.android.trace.TraceReport.Companion.SHUTDOWN_HOOK_REPORT_SOURCE
import com.avito.graph.OperationsPath
import com.avito.logger.LoggerFactory
import org.gradle.BuildResult
import org.gradle.api.Task
import java.io.File
import java.time.Instant
import java.util.Collections
import kotlin.concurrent.thread

internal class BuildTraceListener(
    private val output: File,
    loggerFactory: LoggerFactory,
) : AbstractBuildEventsListener(), CriticalPathListener {

    override val name: String = "BuildTrace"
    private val logger = loggerFactory.create(name)

    private val eventProvider = TraceEventProvider()
    private val events: MutableList<TraceEvent> = Collections.synchronizedList(mutableListOf())
    private val reportWriter = BuildTraceReportWriter(output)

    @Volatile
    private var criticalPath: OperationsPath<TaskOperation>? = null

    private val shutdownHook = thread(
        start = false,
        name = "build-trace-shutdown-hook",
    ) {
        reportWriter.write(
            TraceReport(
                traceEvents = snapshotEvents(),
                metadata = mapOf(REPORT_SOURCE_METADATA_KEY to SHUTDOWN_HOOK_REPORT_SOURCE),
            )
        )
    }

    init {
        Runtime.getRuntime().addShutdownHook(shutdownHook)
    }

    override fun afterExecute(task: Task, state: TaskExecution) {
        events.add(eventProvider.taskExecutionEvent(task, state))
    }

    override fun buildFinished(buildResult: BuildResult, profile: BuildProfile) {
        events.add(eventProvider.initWithConfigurationEvent(profile))
        events.add(eventProvider.executionStartEvent(profile))
        events.add(eventProvider.executionFinishEvent(profile))
        val path = criticalPath
        if (path == null) {
            removeShutdownHook()
        } else {
            writeReport(path)
        }
    }

    override fun onCriticalPathReady(path: OperationsPath<TaskOperation>) {
        logger.info("Critical path ready ${Instant.now()}")
        criticalPath = path
    }

    private fun writeReport(criticalPath: OperationsPath<TaskOperation>) {
        val report = TraceReport(
            traceEvents = enrichCriticalPath(snapshotEvents(), criticalPath),
            metadata = mapOf(REPORT_SOURCE_METADATA_KEY to BUILD_FINISHED_REPORT_SOURCE),
        )
        reportWriter.write(report)
        removeShutdownHook()
        logger.info("Build trace: ${output.path}")
    }

    private fun snapshotEvents(): List<TraceEvent> {
        return synchronized(events) {
            events.toList()
        }
    }

    private fun removeShutdownHook() {
        try {
            Runtime.getRuntime().removeShutdownHook(shutdownHook)
        } catch (_: IllegalStateException) {
            // JVM shutdown has already started; the idempotent hook will observe the finalized report.
        }
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
