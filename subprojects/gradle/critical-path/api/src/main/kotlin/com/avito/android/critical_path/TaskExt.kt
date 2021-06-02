package com.avito.android.critical_path

import com.avito.android.Result
import org.gradle.api.Task
import org.gradle.api.internal.TaskInternal
import org.gradle.api.tasks.TaskDependency

public val Task.type: Class<out Task>
    get() = (this as TaskInternal).taskIdentity.type

public val Task.predecessors: TaskDependenciesResolutionResult
    get() {
        val dependenciesByInputs = project.gradle.taskGraph.getDependencies(this)

        val resolutionResults = listOf(
            taskDependencies.resolveDependencies(this),
            mustRunAfter.resolveDependencies(this),
            shouldRunAfter.resolveDependencies(this)
        )
        val tasks: Set<Task> = dependenciesByInputs +
            resolutionResults.flatMap {
                it.getOrElse { emptySet() }
            }

        val errors = resolutionResults
            .filterIsInstance(Result.Failure::class.java)
            .map { it.throwable }

        return TaskDependenciesResolutionResult.create(tasks, errors)
    }

internal fun TaskDependency.resolveDependencies(task: Task): Result<Set<Task>> =
    Result.tryCatch {
        getDependencies(task)
    }
