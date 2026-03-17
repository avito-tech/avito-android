package com.avito.android.string_transform.internal.task

import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

internal abstract class TransformStringsPipelineVariantMetadataTask : DefaultTask() {

    @get:Input
    abstract val pipelineName: Property<String>

    @get:Input
    abstract val variantName: Property<String>

    @get:Input
    abstract val outputRelativePath: Property<String>

    @get:Input
    abstract val exactRuleCount: Property<Int>

    @get:Input
    abstract val caseExpandedRuleCount: Property<Int>

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
                        "rules" to mapOf(
                            "exactCount" to exactRuleCount.get(),
                            "caseExpandedCount" to caseExpandedRuleCount.get(),
                        ),
                    )
                )
            )
        )
    }
}
