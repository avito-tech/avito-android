package com.avito.android

import com.avito.kotlin.dsl.typedNamed
import org.gradle.api.tasks.TaskContainer

internal const val changedTestsFinderTaskName = "changedTestsFinderTask"

fun TaskContainer.changedTestsFinderTaskProvider() = typedNamed<FindChangedTestsTask>(changedTestsFinderTaskName)
