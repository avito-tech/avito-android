package com.avito.test.summary

import com.avito.kotlin.dsl.typedNamed
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

const val testSummaryPluginId = "com.avito.android.test-summary"

const val testSummaryExtensionName = "testSummary"

internal const val testSummaryTaskName = "testSummary"

fun TaskContainer.testSummaryTask(): TaskProvider<TestSummaryTask> =
    typedNamed(testSummaryTaskName)

internal const val flakyReportTaskName = "flakyReport"

fun TaskContainer.flakyReportTask(): TaskProvider<FlakyReportTask> =
    typedNamed(flakyReportTaskName)
