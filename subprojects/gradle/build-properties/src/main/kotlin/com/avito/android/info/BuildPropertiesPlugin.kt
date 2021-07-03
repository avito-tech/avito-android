package com.avito.android.info

import com.avito.android.addPreBuildTasks
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.WriteProperties
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

public open class BuildPropertiesPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        registerPropertiesTask(project)
        registerLegacyPropertiesTask(project)
    }

    private fun registerPropertiesTask(project: Project) {
        val extension = project.extensions.create<BuildPropertiesExtension>("buildProperties")

        val task = project.tasks.register<WriteProperties>("generateBuildProperties") {
            extension.properties.forEach { (name, value) ->
                property(name, value)
            }
            outputFile = project.file("src/main/assets/build-info.properties")
        }
        project.addPreBuildTasks(task)
    }

    private fun registerLegacyPropertiesTask(project: Project) {
        val extension = project.extensions.create<BuildInfoExtension>("buildInfo")

        val task = project.tasks.register<WriteProperties>("generateAppBuildProperties") {
            property("GIT_COMMIT", extension.gitCommit.orEmpty())
            property("GIT_BRANCH", extension.gitBranch.orEmpty())
            property("BUILD_NUMBER", extension.buildNumber.orEmpty())
            outputFile = project.file("src/main/assets/app-build-info.properties")
        }
        project.addPreBuildTasks(task)
    }
}
