package com.avito.android.string_transform

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.avito.android.string_transform.internal.configuration.TransformPipelineVariantTaskConfigurator
import com.avito.android.string_transform.internal.configuration.TransformPipelinesValidator
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType

@Suppress("UnstableApiUsage")
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
            configureAndroidApplication(
                project = project,
                extension = extension,
                rootTask = rootTask,
            )
        }
    }

    private fun configureAndroidApplication(
        project: Project,
        extension: TransformStringsExtension,
        rootTask: TaskProvider<Task>,
    ) {
        val androidComponents = project.extensions.getByType<ApplicationAndroidComponentsExtension>()
        val pipelines by lazy { validatedPipelines(project, extension) }

        androidComponents.onVariants { variant ->
            pipelines.forEach { pipeline ->
                if (pipeline.variant.orNull == variant.name) {
                    TransformPipelineVariantTaskConfigurator(
                        project = project,
                        rootTask = rootTask,
                        pipeline = pipeline,
                        variant = variant,
                    ).configure()
                }
            }
        }
    }

    private fun validatedPipelines(
        project: Project,
        extension: TransformStringsExtension,
    ): List<TransformPipelineSpec> {
        return extension.pipelines.toList().onEach { pipeline ->
            TransformPipelinesValidator.validate(project.path, pipeline)
        }
    }
}
