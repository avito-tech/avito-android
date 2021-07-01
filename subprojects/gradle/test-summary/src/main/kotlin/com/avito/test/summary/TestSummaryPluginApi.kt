package com.avito.test.summary

import com.avito.kotlin.dsl.typedNamed
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

public const val testSummaryPluginId: String = "com.avito.android.test-summary"

public const val testSummaryExtensionName: String = "testSummary"

internal const val testSummaryTaskName = "testSummary"

internal const val flakyReportTaskName = "flakyReport"

public fun TaskContainer.flakyReportTask(): TaskProvider<FlakyReportTask> =
    typedNamed(flakyReportTaskName)
