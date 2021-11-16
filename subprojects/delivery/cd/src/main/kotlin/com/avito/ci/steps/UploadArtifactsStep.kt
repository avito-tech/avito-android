package com.avito.ci.steps

import com.avito.impact.configuration.internalModule
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

public abstract class UploadArtifactsStep(
    context: String,
    private val artifactsConfiguration: ArtifactsConfiguration,
    name: String
) : BuildStep(context, name),
    ImpactAnalysisAwareBuildStep by ImpactAnalysisAwareBuildStep.Impl(),
    ArtifactAware by ArtifactAwareImpl() {

    override fun registerTask(project: Project, rootTask: TaskProvider<out Task>) {
        if (useImpactAnalysis && !project.internalModule.isModified()) return

        provideTask(project, artifactsConfiguration.outputs.filterKeys { artifacts.contains(it) })
            .forEach { uploadTask ->
                rootTask.configure { it.finalizedBy(uploadTask) }
                uploadTask.configure {
                    it.mustRunAfter(project.tasks.verifyTaskProvider(context))
                }
            }
    }

    protected abstract fun provideTask(
        project: Project,
        artifactsMap: Map<String, Output>
    ): List<TaskProvider<out Task>>
}
