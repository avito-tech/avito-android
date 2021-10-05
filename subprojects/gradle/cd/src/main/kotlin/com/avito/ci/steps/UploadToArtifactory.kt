package com.avito.ci.steps

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.avito.android.androidAppExtension
import com.avito.android.plugin.artifactory.appBackupExtension
import com.avito.android.plugin.artifactory.artifactoryAndroidArtifactsBuildVariants
import com.avito.android.plugin.artifactory.artifactoryAppBackupTask
import com.avito.capitalize
import com.avito.cd.cdBuildConfig
import com.avito.utils.gradle.envArgs
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dslx.noOwnerClosureOf

public class UploadToArtifactory(
    context: String,
    artifactsConfiguration: ArtifactsConfiguration,
    name: String
) : UploadArtifactsStep(context, artifactsConfiguration, name) {

    override fun provideTask(
        project: Project,
        artifactsMap: Map<String, Output>
    ): List<TaskProvider<out Task>> {
        val plugin = "com.avito.android.artifactory-app-backup"

        require(project.plugins.hasPlugin(plugin)) {
            "${this.javaClass.simpleName} check requires $plugin being applied to project"
        }
        val androidAppExtension = project.androidAppExtension
        val defaultConfig = androidAppExtension.defaultConfig
        val projectType = project.cdBuildConfig.orNull?.project?.id ?: "local"
        project.appBackupExtension.backup(
            noOwnerClosureOf {
                name = "${project.name}-android"
                type = projectType
                version = "${defaultConfig.versionName}-${defaultConfig.versionCode}-${project.envArgs.build.number}"
                artifactsMap.forEach { (key, output) ->
                    artifact(
                        noOwnerClosureOf {
                            id = key
                            path = output.path
                        }
                    )
                }
            }
        )

        val backupTask = project.tasks.artifactoryAppBackupTask()

        artifactsMap.forEach { (id, output) ->

            // TODO потому что json с репортами генерятся в тестах
            when (output) {
                is Output.ApkOutput -> {
                    project.artifactoryAndroidArtifactsBuildVariants.put(id, output.variant)
                    backupTask.dependsOn("test${output.variantName.capitalize()}UnitTest")
                }
                is Output.BundleOutput -> {
                    project.artifactoryAndroidArtifactsBuildVariants.put(id, output.variant)
                    backupTask.dependsOn("test${output.variantName.capitalize()}UnitTest")
                }
                else -> {
                }
            }
        }
        project.artifactoryAndroidArtifactsBuildVariants.finalizeValue()
        return listOf(backupTask)
    }
}
