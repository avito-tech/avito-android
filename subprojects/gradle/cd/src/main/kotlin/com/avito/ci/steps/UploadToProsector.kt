package com.avito.ci.steps

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import prosectorTaskProvider
import java.io.File

public class UploadToProsector(
    context: String,
    artifactsConfiguration: ArtifactsConfiguration,
    name: String
) : UploadArtifactsStep(context, artifactsConfiguration, name) {

    override fun provideTask(
        project: Project,
        artifactsMap: Map<String, Output>
    ): List<TaskProvider<out Task>> {
        val plugin = "com.avito.android.prosector"
        require(project.plugins.hasPlugin(plugin)) {
            "${this.javaClass.simpleName} check requires $plugin being applied to project"
        }

        return artifactsMap.map { (_, output) ->
            when (output) {
                is Output.ApkOutput ->
                    project.tasks.prosectorTaskProvider(output.variantName)
                        .apply {
                            configure {
                                it.apk = File(output.path)
                            }
                        }

                else -> error("Prosector upload doesn't support that type of artifact: $output")
            }
        }
    }
}
