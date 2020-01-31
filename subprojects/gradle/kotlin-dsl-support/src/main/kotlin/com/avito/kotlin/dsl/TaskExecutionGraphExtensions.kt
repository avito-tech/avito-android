package com.avito.kotlin.dsl

import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.util.Path

/**
 * Позволяет понять есть ли в графе хотя бы один из указанных тасков
 */
fun TaskExecutionGraph.hasTasks(taskPaths: Collection<Path>): Boolean =
    allTasks.map { it.path }
        .intersect(taskPaths.map { it.path })
        .isNotEmpty()

fun TaskExecutionGraph.hasTask(taskPath: Path): Boolean = hasTask(taskPath.path)
