package com.avito.ci.steps

import com.avito.cd.CdBuildConfig
import com.avito.cd.CdBuildConfig.Deployment
import com.avito.cd.cdBuildConfig
import com.avito.plugin.qappsTaskProvider
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import java.io.File

@Suppress("UnstableApiUsage")
class UploadToQapps(context: String, artifactsConfiguration: ArtifactsConfiguration) :
    UploadArtifactsStep(context, artifactsConfiguration) {

    override fun provideTask(
        project: Project,
        artifactsMap: Map<String, Output>
    ): List<TaskProvider<out Task>> {
        val plugin = "com.avito.android.qapps"
        require(project.plugins.hasPlugin(plugin)) {
            "${this.javaClass.simpleName} check requires $plugin being applied to project"
        }

        val uploadCondition = UploadToQappsCondition(project.cdBuildConfig.orNull)

        return artifactsMap.map { (_, output) ->
            when (output) {
                is Output.ApkOutput ->
                    project.tasks.qappsTaskProvider(output.variantName)
                        .apply {
                            configure {
                                it.getApk().set(File(output.path))
                                it.releaseChain.set(uploadCondition.isReleaseChain())
                                it.onlyIf {
                                    uploadCondition.canUpload()
                                }
                            }
                        }

                else -> error("QApps upload doesn't support that type of artifact: $output")
            }
        }
    }
}

internal class UploadToQappsCondition(
    val config: CdBuildConfig?
) {

    fun isReleaseChain(): Boolean {
        return findDeployment()?.isRelease ?: false
    }

    fun canUpload(): Boolean {
        if (config == null) return true

        return findDeployment() != null
    }

    private fun findDeployment(): Deployment.Qapps? {
        if (config == null) return null

        return config.deployments
            .firstOrNull { it is Deployment.Qapps } as Deployment.Qapps?
    }
}
