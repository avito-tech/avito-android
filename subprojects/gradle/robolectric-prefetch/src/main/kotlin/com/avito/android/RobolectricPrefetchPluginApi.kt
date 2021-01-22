package com.avito.android

import com.avito.kotlin.dsl.typedNamed
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

fun TaskContainer.prefetchTaskProvider(): TaskProvider<RobolectricPrefetchTask> = typedNamed(prefetchTaskName)

internal const val prefetchTaskName = "robolectricFetch"
