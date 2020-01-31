package com.avito.ci.steps

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

        return artifactsMap.map { (_, output) ->
            when (output) {
                is Output.ApkOutput ->
                    project.tasks.qappsTaskProvider(output.variantName)
                        .apply {
                            configure {
                                it.getApk().set(File(output.path))
                            }
                        }

                else -> error("QApps upload doesn't support that type of artifact: $output")
            }
        }
    }
}
