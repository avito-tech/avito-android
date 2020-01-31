package com.avito.android.plugin.build_metrics

import com.avito.android.gradle.profile.TaskExecution
import com.avito.android.trace.CompleteEvent
import com.avito.android.trace.TraceEvent
import org.gradle.api.Task
import org.gradle.api.tasks.TaskDependency
import org.gradle.api.tasks.TaskState
import java.util.concurrent.TimeUnit

internal fun taskExecutionEvent(task: Task, state: TaskExecution): TraceEvent {
    val metadata = mapOf<String, String>(
        "state" to state.state?.toTaskResult().toString(),
        "class" to task.extractClassName(),
        "dependsOn" to task.taskDependencies.dependencyDescription(task),
        "mustRunAfter" to task.mustRunAfter.dependencyDescription(task),
        "shouldRunAfter" to task.shouldRunAfter.dependencyDescription(task),
        "finalizedBy" to task.finalizedBy.dependencyDescription(task)
    ).filterValues { it.isNotBlank() }
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

private fun Task.extractClassName(): String {
    return javaClass.simpleName.removeSuffix("_Decorated")
}

private fun TaskDependency.dependencyDescription(task: Task): String {
    val dependencies = try {
        this.getDependencies(task)
    } catch (error: Exception) { // https://issuetracker.google.com/issues/142268503
        emptySet<Task>()
    }
    return dependencies.joinToString { dependentTask ->
        if (dependentTask.project == task.project) {
            dependentTask.name
        } else {
            dependentTask.path
        }
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

@Suppress("ClassName")
private sealed class TaskResult {
    object EXECUTED : TaskResult()

    class FAILED(val error: Throwable?) : TaskResult() {
        override fun toString(): String {
            return "${this.javaClass.simpleName} ${error.toString()}" // TODO: extract clean stacktrace
        }
    }

    object UP_TO_DATE : TaskResult()

    class SKIPPED(val skipMessage: String) : TaskResult() {
        override fun toString(): String {
            return if (skipMessage == "SKIPPED") {
                this.javaClass.simpleName
            } else {
                "${this.javaClass.simpleName} $skipMessage"
            }
        }
    }

    object NO_SOURCE : TaskResult()

    object UNKNOWN : TaskResult()

    override fun toString(): String {
        return this.javaClass.simpleName
    }
}
