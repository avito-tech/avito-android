package com.avito.android.string_transform.internal.configuration

import Slf4jGradleLoggerFactory
import com.android.build.api.variant.ApplicationVariant
import com.avito.android.string_transform.TransformPipelineSpec
import com.avito.android.string_transform.internal.rules.TransformRulesNormalizer
import com.avito.android.string_transform.internal.task.TransformStringsPipelineVariantMetadataTask
import com.avito.android.string_transform.internal.task.input.TransformRuleInput
import com.avito.capitalize
import com.avito.logger.create
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

internal class TransformPipelineVariantTaskConfigurator(
    private val project: Project,
    private val rootTask: TaskProvider<Task>,
    private val pipeline: TransformPipelineSpec,
    private val variant: ApplicationVariant,
) {

    private val logger = Slf4jGradleLoggerFactory.create<TransformPipelineVariantTaskConfigurator>()

    fun configure() {
        val normalizedRules = TransformRulesNormalizer.normalize(pipeline)
        normalizedRules.warnings.forEach { warning ->
            logger.warn("Pipeline '${pipeline.name}' for variant '${variant.name}': $warning")
        }

        val taskProvider = project.tasks.register<TransformStringsPipelineVariantMetadataTask>(
            variantTaskName(pipeline.name, variant.name)
        ) {
            group = "string transform"
            description = "Produces metadata for ${pipeline.name} pipeline on ${variant.name} variant"

            pipelineName.set(pipeline.name)
            variantName.set(variant.name)
            outputRelativePath.set("outputs/transformStrings/${pipeline.name}/${variant.name}")
            rules.set(
                normalizedRules.rules.map { rule ->
                    TransformRuleInput(
                        from = rule.from,
                        to = rule.to,
                    )
                }
            )
            metadataFile.set(
                project.layout.buildDirectory.file(
                    "outputs/transformStrings/${pipeline.name}/${variant.name}/metadata/pipeline-variant-metadata.json"
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
