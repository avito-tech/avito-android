package com.avito.android

import com.avito.kotlin.dsl.typedNamed
import org.gradle.api.tasks.TaskContainer

internal const val modifiedTestsFinderTaskName = "modifiedTestsFinderTask"

fun TaskContainer.modifiedTestsFinderTaskProvider() = typedNamed<FindModifiedTestsTask>(modifiedTestsFinderTaskName)
