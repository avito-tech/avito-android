package com.avito.instrumentation

import com.avito.kotlin.dsl.typedNamed
import com.avito.kotlin.dsl.typedNamedOrNull
import com.avito.report.model.ReportCoordinates
import com.google.common.annotations.VisibleForTesting
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

internal fun instrumentationTaskName(configuration: String): String =
    "instrumentation${configuration.capitalize()}"

internal fun preInstrumentationTaskName(configuration: String): String =
    "preInstrumentation${configuration.capitalize()}"

internal const val preInstrumentationTaskName: String = "preInstrumentation"

// todo доступен только afterEvaluate и то ненадежно MBS-6926
public fun TaskContainer.instrumentationTask(configuration: String): TaskProvider<InstrumentationTestsTask> =
    typedNamed(instrumentationTaskName(configuration))

@Suppress("UnstableApiUsage")
public fun TaskProvider<InstrumentationTestsTask>.extractReportCoordinates(): Provider<ReportCoordinates> =
    flatMap { task ->
        task.instrumentationConfiguration.map { config ->
            config.instrumentationParams.reportCoordinates()
        }
    }

public fun TaskContainer.preInstrumentationTask(configuration: String): TaskProvider<Task> =
    typedNamed(preInstrumentationTaskName(configuration))

public fun TaskContainer.preInstrumentationTask(): TaskProvider<Task> = named(preInstrumentationTaskName)

public fun TaskContainer.instrumentationTask(
    configuration: String,
    callback: (TaskProvider<InstrumentationTestsTask>) -> Unit
) {
    val name = instrumentationTaskName(configuration)
    val taskProvider = typedNamedOrNull<InstrumentationTestsTask>(name)
    if (taskProvider != null) {
        callback(taskProvider)
    } else {
        whenTaskAdded {
            if (it.name == name) {
                callback.invoke(typedNamed(name))
            }
        }
    }
}

@VisibleForTesting
internal val instrumentationDumpPath = "instrumentation-extension-dump.bin"
