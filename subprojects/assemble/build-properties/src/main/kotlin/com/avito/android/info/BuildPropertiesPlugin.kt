package com.avito.android.info

import com.avito.android.addPreBuildTasks
import com.avito.git.GitStateResult
import com.avito.git.gitInfoService
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
            destinationFile.set(project.file("src/main/assets/build-info.properties"))
        }
        project.addPreBuildTasks(task)
    }

    private fun registerLegacyPropertiesTask(project: Project) {
        val extension = project.extensions.create<BuildInfoExtension>("buildInfo")
        val gitInfo = project.gitInfoService()

        val task = project.tasks.register<WriteProperties>("generateAppBuildProperties") {
            // Explicit values (set via the legacy `buildInfo { gitCommit = ... }` API) win.
            // When unset, resolve from GitInfoBuildService at task-execution time so a
            // `git commit` doesn't invalidate the configuration cache. See MBSA-2353.
            val explicitCommit = extension.gitCommit
            val explicitBranch = extension.gitBranch
            property(
                "GIT_COMMIT",
                gitInfo.map { service ->
                    explicitCommit ?: when (val result = service.getGitStateResult()) {
                        is GitStateResult.Available -> result.state.currentBranch.commit
                        is GitStateResult.Unavailable -> ""
                    }
                }
            )
            property(
                "GIT_BRANCH",
                gitInfo.map { service ->
                    explicitBranch ?: when (val result = service.getGitStateResult()) {
                        is GitStateResult.Available -> result.state.currentBranch.name
                        is GitStateResult.Unavailable -> ""
                    }
                }
            )
            property("BUILD_NUMBER", extension.buildNumber.orEmpty())
            destinationFile.set(project.file("src/main/assets/app-build-info.properties"))
        }
        project.addPreBuildTasks(task)
    }
}
