package com.avito.instrumentation

import com.avito.kotlin.dsl.typedNamed
import com.avito.report.model.ReportCoordinates
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import java.util.Locale

internal fun instrumentationTaskName(configuration: String): String =
    "instrumentation${configuration.capitalize(Locale.getDefault())}"

/**
 * Available only afterEvaluate MBS-6926
 */
public fun TaskContainer.instrumentationTask(configuration: String): TaskProvider<InstrumentationTestsTask> =
    typedNamed(instrumentationTaskName(configuration))

public fun TaskProvider<InstrumentationTestsTask>.extractReportCoordinates(): Provider<ReportCoordinates> =
    @Suppress("UnstableApiUsage")
    flatMap { task ->
        task.instrumentationConfiguration.map { config ->
            config.instrumentationParams.reportCoordinates()
        }
    }
