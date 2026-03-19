package com.avito.android.string_transform.internal.task

import com.avito.android.string_transform.internal.task.input.TransformRuleInput
import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

internal abstract class TransformStringsPipelineVariantMetadataTask : DefaultTask() {

    @get:Input
    abstract val pipelineName: Property<String>

    @get:Input
    abstract val variantName: Property<String>

    @get:Input
    abstract val outputRelativePath: Property<String>

    @get:Nested
    abstract val rules: ListProperty<TransformRuleInput>

    @get:OutputFile
    abstract val metadataFile: RegularFileProperty

    @TaskAction
    fun writeMetadata() {
        val output = metadataFile.get().asFile
        output.parentFile.mkdirs()
        output.writeText(
            JsonOutput.prettyPrint(
                JsonOutput.toJson(
                    mapOf(
                        "pipeline" to pipelineName.get(),
                        "variant" to variantName.get(),
                        "outputRelativePath" to outputRelativePath.get(),
                        "rules" to rules.get().map { rule ->
                            mapOf(
                                "from" to rule.from,
                                "to" to rule.to,
                            )
                        },
                    )
                )
            )
        )
    }
}
