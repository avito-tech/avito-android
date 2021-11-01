package com.avito.instrumentation

import com.avito.capitalize
import com.avito.kotlin.dsl.typedNamed
import com.avito.reportviewer.model.ReportCoordinates
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

internal fun instrumentationTaskName(
    configuration: String,
    environment: String,
    flavor: String?
): String =
    buildString {
        append("instrumentation")
        if (!flavor.isNullOrBlank()) {
            append(flavor.capitalize())
        }
        append(configuration.capitalize())
        if (environment.isNotBlank()) {
            append(environment.capitalize())
        }
    }

public fun TaskContainer.instrumentationTaskDefaultEnvironment(
    configuration: String,
    flavor: String?
): TaskProvider<InstrumentationTestsTask> =
    typedNamed(instrumentationTaskName(configuration, ENVIRONMENT_DEFAULT, flavor))

public fun TaskProvider<InstrumentationTestsTask>.extractReportCoordinates(): Provider<ReportCoordinates> =
    flatMap { task ->
        task.instrumentationConfiguration.map { config ->
            config.instrumentationParams.reportCoordinates()
        }
    }

public fun TaskProvider<InstrumentationTestsTask>.extractReportViewerUrl(): Provider<String> =
    flatMap { task ->
        task.reportViewerProperty.map { reportViewer ->
            reportViewer.reportViewerUrl
        }
    }

public const val instrumentationPluginId: String = "com.avito.android.instrumentation-tests"

internal const val dumpDirName: String = "input-args-dump"

internal const val CI_TASK_GROUP = "ci"

internal const val ENVIRONMENT_DEFAULT: String = "default"
