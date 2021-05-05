package com.avito.android.build_trace.internal

import com.avito.android.build_trace.internal.critical_path.TaskOperation
import com.avito.android.gradle.profile.BuildProfile
import com.avito.android.gradle.profile.TaskExecution
import com.avito.android.trace.CompleteEvent
import com.avito.android.trace.InstantEvent
import com.avito.android.trace.TraceEvent
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskState
import java.util.concurrent.TimeUnit

internal class TraceEventProvider {

    fun taskExecutionEvent(task: Task, state: TaskExecution): TraceEvent {
        val predecessorTasks = task.predecessors
            .map {
                taskShortDescription(it, task.project)
            }
        val finalizeTasks = task.finalizedBy
            .safeDependencies(task)
            .map {
                taskShortDescription(it, task.project)
            }

        val metadata = mapOf<String, String>(
            "state" to state.state?.toTaskResult().toString(),
            "type" to task.type.simpleName,
            "predecessors" to predecessorTasks.toString(),
            "finalizedBy" to finalizeTasks.toString(),
        )
            .filterValues { it.isNotBlank() }

        return CompleteEvent(
            timestampMicroseconds = TimeUnit.MILLISECONDS.toMicros(state.startTime),
            durationMicroseconds = TimeUnit.MILLISECONDS.toMicros(state.elapsedTime),
            processId = unknownProcessId,
            threadId = threadId(),
            eventName = task.path,
            color = state.extractColor(),
            args = metadata
        )
    }

    fun initWithConfigurationEvent(profile: BuildProfile) = CompleteEvent(
        timestampMicroseconds = TimeUnit.MILLISECONDS.toMicros(profile.profilingStarted),
        durationMicroseconds = TimeUnit.MILLISECONDS.toMicros(profile.initWithConfigurationTimeMs),
        processId = unknownProcessId,
        threadId = unknownThreadId,
        eventName = "init + configuration",
        color = TraceEvent.COLOR_GOOD
    )

    fun executionStartEvent(profile: BuildProfile) = InstantEvent(
        timestampMicroseconds = TimeUnit.MILLISECONDS.toMicros(
            profile.profilingStarted + profile.initWithConfigurationTimeMs
        ),
        scope = InstantEvent.SCOPE_GLOBAL,
        processId = unknownProcessId,
        threadId = unknownThreadId,
        eventName = "execution start"
    )

    fun executionFinishEvent(profile: BuildProfile) = InstantEvent(
        timestampMicroseconds = TimeUnit.MILLISECONDS.toMicros(
            profile.profilingStarted + profile.elapsedTotal
        ),
        scope = InstantEvent.SCOPE_GLOBAL,
        processId = unknownProcessId,
        threadId = unknownThreadId,
        eventName = "execution end"
    )

    fun criticalPathEvent(event: TraceEvent, path: List<TaskOperation>): TraceEvent {
        if (event !is CompleteEvent) return event

        val inPath = path.firstOrNull { it.path == event.eventName } != null
        return if (inPath) {
            event.copy(color = TraceEvent.COLOR_YELLOW)
        } else {
            event
        }
    }

    private fun taskShortDescription(task: Task, project: Project): String {
        return if (project == task.project) {
            task.name
        } else {
            task.path
        }
    }

    private fun TaskExecution.extractColor(): String {
        val state = state
        require(state != null)

        return when {
            state.failure != null -> TraceEvent.COLOR_TERRIBLE
            else -> TraceEvent.COLOR_GOOD
        }
    }

    private fun threadId(): String {
        val workerNumber = Thread.currentThread().name.substringAfter(" Thread ")
        return if (workerNumber.contains("Execution worker for ':'")) {
            unknownThreadId
        } else {
            workerNumber
        }
    }

    private fun TaskState.toTaskResult(): TaskResult {
        return when {
            this.noSource -> TaskResult.NO_SOURCE
            this.upToDate -> TaskResult.UP_TO_DATE
            this.skipped -> TaskResult.SKIPPED(requireNotNull(this.skipMessage))
            this.failure != null -> TaskResult.FAILED(this.failure)
            this.executed -> TaskResult.EXECUTED
            else -> TaskResult.UNKNOWN
        }
    }
}

private const val unknownProcessId = "_"
private const val unknownThreadId = "_"
