package com.avito.kotlin.dsl

import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.util.Path

fun TaskExecutionGraph.hasTasks(taskPaths: Collection<Path>): Boolean {
    return allTasks.map { it.path }
        .intersect(taskPaths.map { it.path })
        .isNotEmpty()
}

fun TaskExecutionGraph.hasTask(taskPath: Path): Boolean = hasTask(taskPath.path)
