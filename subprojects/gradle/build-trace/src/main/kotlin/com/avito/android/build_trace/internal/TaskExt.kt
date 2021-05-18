package com.avito.android.build_trace.internal

import org.gradle.api.Task
import org.gradle.api.internal.TaskInternal

internal val Task.type: Class<out Task>
    get() = (this as TaskInternal).taskIdentity.type

internal val Task.predecessors: Set<Task>
    get() {
        val dependenciesByInputs = project.gradle.taskGraph.getDependencies(this)
        return (
            dependenciesByInputs +
                taskDependencies.getDependencies(this) +
                mustRunAfter.getDependencies(this) +
                shouldRunAfter.getDependencies(this)
            )
            .toSet()
    }
