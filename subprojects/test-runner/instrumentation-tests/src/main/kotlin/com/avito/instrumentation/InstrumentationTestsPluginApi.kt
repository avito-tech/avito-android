package com.avito.instrumentation

import com.avito.capitalize
import com.avito.kotlin.dsl.typedNamed
import com.avito.reportviewer.model.ReportCoordinates
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

internal fun instrumentationTaskName(configuration: String, flavor: String?): String {
    return buildString {
        append("instrumentation")
        if (!flavor.isNullOrBlank()) {
            append(flavor.capitalize())
        }
        append(configuration.capitalize())
    }
}

/**
 * Available only afterEvaluate MBS-6926
 */
public fun TaskContainer.instrumentationTask(
    configuration: String,
    flavor: String?
): TaskProvider<InstrumentationTestsTask> = typedNamed(instrumentationTaskName(configuration, flavor))

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
