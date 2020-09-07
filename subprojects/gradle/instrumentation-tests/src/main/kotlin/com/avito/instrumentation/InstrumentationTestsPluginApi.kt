package com.avito.instrumentation

import com.avito.instrumentation.configuration.InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration
import com.avito.instrumentation.configuration.withInstrumentationExtensionData
import com.avito.kotlin.dsl.typedNamed
import com.avito.kotlin.dsl.typedNamedOrNull
import com.google.common.annotations.VisibleForTesting
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.withType

internal fun instrumentationTaskName(configuration: String): String =
    "instrumentation${configuration.capitalize()}"

internal fun preInstrumentationTaskName(configuration: String): String =
    "preInstrumentation${configuration.capitalize()}"

internal const val preInstrumentationTaskName: String = "preInstrumentation"

//todo доступен только afterEvaluate и то ненадежно MBS-6926
fun TaskContainer.instrumentationTask(configuration: String): TaskProvider<InstrumentationTestsTask> =
    typedNamed(instrumentationTaskName(configuration))

fun TaskContainer.preInstrumentationTask(configuration: String): TaskProvider<Task> =
    typedNamed(preInstrumentationTaskName(configuration))

fun TaskContainer.preInstrumentationTask(): TaskProvider<Task> = named(preInstrumentationTaskName)

fun TaskContainer.instrumentationTask(
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

fun Project.withInstrumentationTests(block: (config: GradleInstrumentationPluginConfiguration.Data) -> Unit) {
    plugins.withType<InstrumentationTestsPlugin> {
        withInstrumentationExtensionData(block)
    }
}

@VisibleForTesting
internal val instrumentationDumpPath = "instrumentation-extension-dump.bin"
