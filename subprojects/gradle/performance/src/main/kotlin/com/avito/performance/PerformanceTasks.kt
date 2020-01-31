package com.avito.performance

import com.avito.kotlin.dsl.typedNamed
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

internal fun measurePerformanceTaskName(configuration: String): String = "measure${configuration.capitalize()}"

fun TaskContainer.measurePerformanceTask(configuration: String): TaskProvider<Task> =
    typedNamed(measurePerformanceTaskName(configuration))
