package com.avito.android.build_checks.internal

import org.gradle.api.Project
import org.gradle.tooling.BuildException

internal class RequiredPluginsChecker(
    private val project: Project
) {

    private val validationErrors = mutableListOf<String>()

    fun check() {
        project.subprojects { subproject ->
            subproject.afterEvaluate {
                subproject.plugins.withId("com.android.application") {
                    subproject.checkAppliesRequiredPlugin("kotlin-android")
                }
                subproject.plugins.withId("com.android.library") {
                    subproject.checkAppliesRequiredPlugin("kotlin-android")
                    subproject.checkAppliesRequiredPlugin("com.avito.android.module-types")
                }
                subproject.plugins.withId("kotlin") {
                    subproject.checkAppliesRequiredPlugin("com.avito.android.module-types")
                }
                subproject.plugins.withId("org.jetbrains.kotlin.jvm") {
                    subproject.checkAppliesRequiredPlugin("com.avito.android.module-types")
                }
            }
        }
        showErrorsIfAny(project)
    }

    private fun Project.checkAppliesRequiredPlugin(pluginId: String) {
        lazyCheck(plugins.hasPlugin(pluginId)) {
            "You forgot to apply '$pluginId' plugin to kotlin library module $path. it is required"
        }
    }

    private fun lazyCheck(precondition: Boolean, message: () -> String) {
        if (!precondition) {
            validationErrors += message.invoke()
        }
    }

    private fun showErrorsIfAny(project: Project) {
        project.gradle.projectsEvaluated {
            if (validationErrors.isNotEmpty()) {
                throw BuildException(
                    "There were errors:\n" +
                        validationErrors.joinToString(separator = "\n", transform = { " - $it" }),
                    null
                )
            }
        }
    }
}
