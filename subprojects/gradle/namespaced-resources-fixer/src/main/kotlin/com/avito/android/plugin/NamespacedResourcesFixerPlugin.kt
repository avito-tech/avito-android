package com.avito.android.plugin

import com.android.build.gradle.internal.tasks.MergeJavaResourceTask
import com.avito.android.withAndroidApp
import com.avito.impact.configuration.internalModule
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.kotlin.dsl.isRoot
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

/**
 * Migrates unambiguous non-namespaced R classes to namespaced ones (android.namespacedRClass)
 *
 * This is a proof of concept. It will be deleted or replaced by an IDE plugin later.
 *
 * Usage:
 * ./gradlew fixNamespacedResources --Pavito.fixNamespacedResources.filesPrefix=avito/src/androidTest/kotlin/com/avito/android/ScreenElement.kt
 *
 */
open class NamespacedResourcesFixerPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        require(project.isRoot()) {
            "Plugin must be applied to the root project"
        }
        check(project.pluginManager.hasPlugin("com.avito.android.impact")) {
            "'com.avito.android.namespacedResourcesFixer' plugin requires 'com.avito.android.impact' plugin"
        }

        val filesPrefix: String =
            project.getOptionalStringProperty(FILES_PREFIX_PROPERTY, nullIfBlank = true) ?: return

        val file = project.file(filesPrefix)

        val fixTask = project.tasks.register("fixNamespacedResources", FixNamespacedResourcesTask::class.java) {
            require(file.exists()) {
                "File $filesPrefix should exist"
            }
            it.filesPath.set(filesPrefix)
        }
        project.subprojects
            .forEach { subProject ->
                subProject.withAndroidApp {
                    if (file.toPath().startsWith(subProject.projectDir.toPath())) {
                        configureTask(fixTask, subProject)
                    }
                }
            }
    }

    private fun configureTask(fixTask: TaskProvider<FixNamespacedResourcesTask>, app: Project) {
        fixTask.configure {
            it.modulePath.set(app.path)
        }
        app.gradle.projectsEvaluated {
            app.internalModule
                .configurations
                .flatMap { it.allDependencies() }
                .forEach {
                    it.module.project.tasks.withType(MergeJavaResourceTask::class.java).all { resourceTask ->
                        fixTask.configure {
                            it.dependsOn(resourceTask)
                        }
                    }
                }
        }
    }
}

internal const val FILES_PREFIX_PROPERTY = "avito.fixNamespacedResources.filesPrefix"
