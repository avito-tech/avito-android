package com.avito.android.build_trace.internal

import com.avito.android.gradle.metric.AbstractBuildEventsListener
import com.avito.android.gradle.profile.BuildProfile
import com.avito.android.gradle.profile.TaskExecution
import com.avito.android.trace.TraceEvent
import com.avito.android.trace.TraceReport
import com.avito.android.trace.TraceReportClient
import com.avito.logger.GradleLoggerFactory
import com.avito.logger.Logger
import com.avito.logger.create
import org.gradle.BuildResult
import org.gradle.api.Project
import org.gradle.api.Task
import java.io.File
import java.util.Collections

internal class BuildTraceListener(
    private val output: File,
    private val logger: Logger
) : AbstractBuildEventsListener() {

    private val eventProvider = TraceEventProvider()
    private val events: MutableList<TraceEvent> = Collections.synchronizedList(mutableListOf())

    override fun afterExecute(task: Task, state: TaskExecution) {
        events.add(eventProvider.taskExecutionEvent(task, state))
    }

    override fun buildFinished(buildResult: BuildResult, profile: BuildProfile) {
        events.add(eventProvider.initWithConfigurationEvent(profile))
        events.add(eventProvider.executionStartEvent(profile))
        events.add(eventProvider.executionFinishEvent(profile))

        writeReport()
    }

    private fun writeReport() {
        output.parentFile.mkdirs()

        val report = TraceReport(traceEvents = events)
        TraceReportClient().writeTo(output, report)
        logger.info("Build trace: ${output.path}")
    }

    companion object {

        fun from(project: Project, loggerFactory: GradleLoggerFactory): BuildTraceListener {
            return BuildTraceListener(
                output = File(project.projectDir, "outputs/avito/build-trace/build.trace"),
                logger = loggerFactory.create<BuildTraceListener>()
            )
        }
    }
}
