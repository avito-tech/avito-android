package com.avito.android.info

import com.avito.android.addPreBuildTasks
import com.avito.git.Branch
import com.avito.git.GitInfoBuildService
import com.avito.git.GitStateResult
import com.avito.git.gitInfoService
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
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
            property("GIT_COMMIT", project.providers.gitValue(extension.gitCommit, gitInfo) { it.commit })
            property("GIT_BRANCH", project.providers.gitValue(extension.gitBranch, gitInfo) { it.name })
            property("BUILD_NUMBER", extension.buildNumber.orEmpty())
            destinationFile.set(project.file("src/main/assets/app-build-info.properties"))
        }
        project.addPreBuildTasks(task)
    }

    private fun ProviderFactory.gitValue(
        value: GitValue,
        gitInfo: Provider<GitInfoBuildService>,
        fromBranch: (Branch) -> String,
    ): Provider<String> = when (value) {
        GitValue.FromGit -> gitInfo.map { service ->
            when (val result = service.getGitStateResult()) {
                is GitStateResult.Available -> fromBranch(result.state.currentBranch)
                is GitStateResult.Unavailable -> ""
            }
        }
        GitValue.Omitted -> provider { "" }
        is GitValue.Explicit -> provider { value.value }
    }
}
