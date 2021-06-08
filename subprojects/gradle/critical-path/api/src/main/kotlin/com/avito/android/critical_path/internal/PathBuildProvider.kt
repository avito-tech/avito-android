package com.avito.android.critical_path.internal

import com.avito.android.Problem
import com.avito.android.asRuntimeException
import com.avito.android.critical_path.CriticalPathListener
import com.avito.android.critical_path.TaskDependenciesResolutionResult
import com.avito.android.critical_path.TaskOperation
import com.avito.android.critical_path.predecessors
import com.avito.android.critical_path.type
import com.avito.android.gradle.metric.AbstractBuildEventsListener
import com.avito.android.gradle.profile.BuildProfile
import com.avito.android.gradle.profile.TaskExecution
import com.avito.graph.OperationsPath
import com.avito.graph.ShortestPath
import org.gradle.BuildResult
import org.gradle.api.Task
import org.gradle.util.Path

/**
 * Using an obsolete TaskExecutionListener instead of OperationCompletionListener
 * due to https://github.com/gradle/gradle/issues/15824
 */
internal class PathBuildProvider : AbstractBuildEventsListener() {

    private val operations = mutableSetOf<TaskOperation>()
    private val listeners = mutableListOf<CriticalPathListener>()

    private val criticalPath: OperationsPath<TaskOperation> by lazy {
        val shortestPath = ShortestPath(operations).find()

        OperationsPath(
            shortestPath.operations.map { it.invertTime() }
        )
    }

    fun addPathListener(listener: CriticalPathListener) {
        listeners.add(listener)
    }

    override fun afterExecute(task: Task, state: TaskExecution) {
        operations.add(
            taskOperation(task, state)
                .invertTime()
        )
    }

    override fun buildFinished(buildResult: BuildResult, profile: BuildProfile) {
        listeners.forEach {
            it.onCriticalPathReady(criticalPath)
        }
    }

    private fun taskOperation(task: Task, state: TaskExecution): TaskOperation {
        return when (val resolution = task.predecessors) {
            is TaskDependenciesResolutionResult.Failed -> throwDependencyResolutionProblem(
                task,
                resolution
            )
            is TaskDependenciesResolutionResult.Success -> TaskOperation(
                path = Path.path(task.path),
                type = task.type,
                startMs = state.startTime,
                finishMs = state.finish,
                predecessors = resolution.tasks.map { it.path }.toSet()
            )
        }
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
}

/**
 * To use the shortest path algorithm as the longest path
 */
private fun TaskOperation.invertTime() = copy(
    startMs = -startMs,
    finishMs = -finishMs
)
