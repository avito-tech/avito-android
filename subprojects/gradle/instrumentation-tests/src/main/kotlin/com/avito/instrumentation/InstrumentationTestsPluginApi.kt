package com.avito.instrumentation

import com.avito.instrumentation.configuration.InstrumentationPluginConfiguration
import com.avito.kotlin.dsl.typedNamed
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.withType

//API для использования другими плагинами

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
    whenTaskAdded {
        if (it.name == name) {
            callback.invoke(typedNamed(name))
        }
    }
}

fun Project.withInstrumentationTests(block: (config: InstrumentationPluginConfiguration.Data) -> Unit) {
    plugins.withType<InstrumentationTestsPlugin> {
        val pluginConfiguration =
            extensions.findByType<InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration>()
        requireNotNull(pluginConfiguration) { "InstrumentationTestsPlugin not found!" }

        InstrumentationPluginConfiguration(
            project = project,
            configuration = pluginConfiguration
        ).withData(block)
    }
}
