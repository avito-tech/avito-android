package com.avito.android.build_trace.internal

import com.avito.android.Problem
import com.avito.android.asRuntimeException
import com.avito.android.critical_path.TaskDependenciesResolutionResult
import com.avito.android.critical_path.TaskOperation
import com.avito.android.critical_path.predecessors
import com.avito.android.critical_path.type
import com.avito.android.gradle.profile.BuildProfile
import com.avito.android.gradle.profile.TaskExecution
import com.avito.android.trace.CompleteEvent
import com.avito.android.trace.InstantEvent
import com.avito.android.trace.TraceEvent
import com.avito.graph.OperationsPath
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskState
import java.util.concurrent.TimeUnit

internal class TraceEventProvider {

    fun taskExecutionEvent(task: Task, state: TaskExecution): TraceEvent {
        val predecessorTasks = when (val resolutionResult = task.predecessors) {
            is TaskDependenciesResolutionResult.Success -> resolutionResult.tasks
                .map {
                    taskShortDescription(it, task.project)
                }
            is TaskDependenciesResolutionResult.Failed -> throwDependencyResolutionProblem(task, resolutionResult)
        }

        val finalizeTasks = task.finalizedBy
            .getDependencies(task)
            .map {
                taskShortDescription(it, task.project)
            }

        val metadata = mutableMapOf<String, String>(
            "state" to state.state?.toTaskResult().toString(),
            "type" to task.type.name
        )
        if (predecessorTasks.isNotEmpty()) {
            metadata["predecessors"] = predecessorTasks.joinToString()
        }
        if (finalizeTasks.isNotEmpty()) {
            metadata["finalizedBy"] = finalizeTasks.joinToString()
        }

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

    private fun throwDependencyResolutionProblem(
        task: Task,
        result: TaskDependenciesResolutionResult.Failed
    ): Nothing {
        throw Problem.Builder(
            shortDescription = "Can't find predecessors for task ${task.path}",
            context = "Calculating build critical path"
        )
            .addSolution("Disable a plugin")
            .addSolution(
                "Suppress a specific error explicitly. " +
                    "See TaskDependenciesResolutionResult implementation."
            )
            .throwable(result.problem)
            .build()
            .asRuntimeException()
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

    fun criticalPathEvent(event: TraceEvent, path: OperationsPath<TaskOperation>): TraceEvent {
        if (event !is CompleteEvent) return event

        val inPath = path.operations.firstOrNull { it.path.toString() == event.eventName } != null
        return if (inPath) {
            event.copy(
                color = TraceEvent.COLOR_YELLOW,
                args = event.args.orEmpty() + mapOf(argCriticalPath to true)
            )
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

private const val argCriticalPath = "CRITICAL_PATH"
private const val unknownProcessId = "_"
private const val unknownThreadId = "_"
