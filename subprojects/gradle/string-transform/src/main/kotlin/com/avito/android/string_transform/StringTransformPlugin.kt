package com.avito.android.string_transform

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import com.avito.android.string_transform.internal.task.TransformStringsPipelineVariantMetadataTask
import com.avito.capitalize
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

public class StringTransformPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create<TransformStringsExtension>(
            "transformStrings",
            project.path,
            project.objects,
        )

        project.pluginManager.withPlugin("com.android.application") {
            val rootTask = project.tasks.register("transformStrings") {
                it.group = "string transform"
                it.description = "Runs all registered string-transform pipelines for the module"
            }
            configureAndroidApplication(project, extension, rootTask)
        }
    }

    private fun configureAndroidApplication(
        project: Project,
        extension: TransformStringsExtension,
        rootTask: TaskProvider<Task>,
    ) {
        val androidComponents = project.extensions.getByType<ApplicationAndroidComponentsExtension>()

        androidComponents.onVariants { variant ->
            val variantSpecific = extension.pipelines.filter { it.variant.orNull == variant.name }
            variantSpecific.forEach { pipeline ->
                registerPipelineVariantTask(project, variant, rootTask, pipeline)
            }
        }
    }

    private fun registerPipelineVariantTask(
        project: Project,
        variant: ApplicationVariant,
        rootTask: TaskProvider<Task>,
        pipeline: TransformPipelineSpec,
    ) {
        val pipelineName = pipeline.name
        val taskProvider = project.tasks.register<TransformStringsPipelineVariantMetadataTask>(
            variantTaskName(
                pipelineName,
                variant.name
            )
        ) {
            group = "string transform"
            description = "Produces metadata for $pipelineName pipeline on ${variant.name} variant"

            this.pipelineName.set(pipelineName)
            this.variantName.set(variant.name)
            this.outputRelativePath.set("outputs/transformStrings/$pipelineName/${variant.name}")
            this.exactRuleCount.set(pipeline.rules.exactRuleCount)
            this.caseExpandedRuleCount.set(pipeline.rules.caseExpandedRuleCount)

            metadataFile.set(
                project.layout.buildDirectory.file(
                    "outputs/transformStrings/$pipelineName/${variant.name}/metadata/pipeline-variant-metadata.json"
                )
            )
        }

        rootTask.configure {
            it.dependsOn(taskProvider)
        }
    }

    private fun variantTaskName(pipelineName: String, variantName: String): String {
        return "transformStrings${pipelineName.capitalize()}${variantName.capitalize()}"
    }
}
