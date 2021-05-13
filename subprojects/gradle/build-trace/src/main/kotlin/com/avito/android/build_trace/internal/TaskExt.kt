package com.avito.android.build_trace.internal

import org.gradle.api.Task
import org.gradle.api.internal.TaskInternal
import org.gradle.api.tasks.TaskDependency

internal val Task.type: Class<out Task>
    get() = (this as TaskInternal).taskIdentity.type

internal val Task.predecessors: Set<Task>
    get() {
        val dependenciesByInputs = project.gradle.taskGraph.getDependencies(this)
        return (
            dependenciesByInputs +
                taskDependencies.safeDependencies(this) +
                mustRunAfter.safeDependencies(this) +
                shouldRunAfter.safeDependencies(this)
            )
            .toSet()
    }

internal fun TaskDependency.safeDependencies(task: Task): Set<Task> {
    return try {
        this.getDependencies(task)
    } catch (error: Exception) {
        // https://issuetracker.google.com/issues/142268503
        emptySet()
    }
}
