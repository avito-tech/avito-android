package com.avito.kotlin.dsl

import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.util.Path

public fun TaskExecutionGraph.hasTasks(taskPaths: Collection<Path>): Boolean {
    return allTasks.map { it.path }
        .intersect(taskPaths.map { it.path })
        .isNotEmpty()
}

public fun TaskExecutionGraph.hasTask(taskPath: Path): Boolean = hasTask(taskPath.path)
