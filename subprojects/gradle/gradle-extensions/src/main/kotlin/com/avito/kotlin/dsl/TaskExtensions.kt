@file:Suppress("UnstableApiUsage")

package com.avito.kotlin.dsl

import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.invoke

/**
 * Adds "dependsOn" explicitly to make relationships more obvious
 */
inline fun <reified T : Task> Task.dependencyOn(anotherTaskProvider: TaskProvider<T>, configuration: (T) -> Unit) {
    configuration.invoke(anotherTaskProvider.get())
    dependsOn(anotherTaskProvider)
}

inline fun <reified T : Task> Task.dependencyOn(anotherTask: T, configuration: (T) -> Unit) {
    configuration.invoke(anotherTask)
    dependsOn(anotherTask)
}

/**
 * Add Task dependencies based on a project plugins
 * @param project pairs of pluginId, task.name (ex: "com.android.application" to "testDebugUnitTest")
 * @param excludeModulePaths full path, e.g. :module:submodule of module to be excluded in task entirely
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <T : Task> NamedDomainObjectProvider<T>.dependsOnProjectsTasks(
    project: Project,
    vararg pluginToTask: Pair<String, String>,
    excludeModulePaths: Set<String> = emptySet()
) {
    if (excludeModulePaths.contains(project.path)) return
    pluginToTask.forEach { (pluginId, task) ->
        project.plugins.withId(pluginId) {
            this@dependsOnProjectsTasks { dependsOn("${project.path}:$task") }
        }
    }
}

/**
 * Add Task finalizeBy rule based on a project plugins
 * @param project pairs of pluginId, task.name (ex: "com.android.application" to "testDebugUnitTest")
 * @param excludeModulePaths full path, e.g. :module:submodule of module to be excluded in task entirely
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <T : Task> NamedDomainObjectProvider<T>.finalizeByProjectsTasks(
    project: Project,
    vararg pluginToTask: Pair<String, String>,
    excludeModulePaths: Set<String> = emptySet()
) {
    if (excludeModulePaths.contains(project.path)) return
    pluginToTask.forEach { (pluginId, task) ->
        project.plugins.withId(pluginId) {
            this { finalizedBy("${project.path}:$task") }
        }
    }
}
