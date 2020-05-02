package com.avito.android.build_trace

import com.avito.android.gradle.metric.AbstractMetricsConsumer
import com.avito.android.gradle.profile.BuildProfile
import com.avito.android.gradle.profile.TaskExecution
import com.avito.android.trace.CompleteEvent
import com.avito.android.trace.InstantEvent
import com.avito.android.trace.TraceEvent
import com.avito.android.trace.TraceReport
import com.avito.android.trace.TraceReportClient
import com.avito.utils.logging.CILogger
import org.gradle.BuildResult
import org.gradle.api.Task
import java.io.File
import java.util.Collections
import java.util.concurrent.TimeUnit

internal class BuildTraceConsumer(
    private val output: File,
    private val logger: CILogger
) : AbstractMetricsConsumer() {

    private val events: MutableList<TraceEvent> = Collections.synchronizedList(mutableListOf())

    override fun afterExecute(task: Task, state: TaskExecution) {
        events.add(taskExecutionEvent(task, state))
    }

    override fun buildFinished(buildResult: BuildResult, profile: BuildProfile) {
        output.parentFile.mkdirs()

        events.addAll(configurationEvents(profile))
        events.add(buildFinishEvent(profile))

        val report = TraceReport(traceEvents = events)
        TraceReportClient().writeTo(output, report)
        logger.info("Build trace: ${output.path}")
    }

    private fun configurationEvents(profile: BuildProfile): List<TraceEvent> {
        val duration = CompleteEvent(
            timestampMicroseconds = TimeUnit.MILLISECONDS.toMicros(profile.profilingStarted),
            durationMicroseconds = TimeUnit.MILLISECONDS.toMicros(profile.initWithConfigurationTimeMs),
            processId = unknownProcessId,
            threadId = unknownThreadId,
            eventName = "init + configuration",
            color = TraceEvent.COLOR_GOOD
        )
        val marker = InstantEvent(
            timestampMicroseconds = duration.timestampMicroseconds + duration.durationMicroseconds,
            scope = InstantEvent.SCOPE_GLOBAL,
            processId = unknownProcessId,
            threadId = unknownThreadId,
            eventName = "execution start"
        )
        return listOf(duration, marker)
    }

    private fun buildFinishEvent(profile: BuildProfile) = InstantEvent(
        timestampMicroseconds = TimeUnit.MILLISECONDS.toMicros(profile.profilingStarted + profile.elapsedTotal),
        scope = InstantEvent.SCOPE_GLOBAL,
        processId = unknownProcessId,
        threadId = unknownThreadId,
        eventName = "execution end"
    )

}

internal const val unknownProcessId = "_"
internal const val unknownThreadId = "_"
