package com.avito.android.string_transform.internal.task

import com.avito.android.string_transform.internal.report.TransformReport
import com.avito.android.string_transform.internal.report.TransformReportJsonWriter
import com.avito.android.string_transform.internal.report.TransformReportRecorder
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

internal abstract class TransformStringsPipelineVariantReportTask : DefaultTask() {

    @get:Input
    abstract val modulePath: Property<String>

    @get:Input
    abstract val pipelineName: Property<String>

    @get:Input
    abstract val variantName: Property<String>

    @get:Input
    abstract val totalRules: Property<Int>

    @get:Input
    abstract val exactRuleCount: Property<Int>

    @get:Input
    abstract val caseExpandedRuleCount: Property<Int>

    @get:Input
    abstract val configurationWarnings: ListProperty<String>

    @get:OutputFile
    abstract val reportFile: RegularFileProperty

    @TaskAction
    fun writeReport() {
        val recorder = TransformReportRecorder(
            modulePath = modulePath.get(),
            pipelineName = pipelineName.get(),
            variantName = variantName.get(),
            rules = TransformReport.Rules(
                totalRules = totalRules.get(),
                declarationCounts = TransformReport.DeclarationCounts(
                    exact = exactRuleCount.get(),
                    caseExpanded = caseExpandedRuleCount.get(),
                )
            ),
            configurationWarnings = configurationWarnings.get(),
        )
        val executionResult = recorder.recordPhase("stub-processing", ::runStubProcessing)
        val writeResult = TransformReportJsonWriter.write(
            report = recorder.build(),
            outputFile = reportFile.get().asFile,
        )

        executionResult
            .combine(writeResult) { _, _ -> }
            .getOrThrow()
    }

    // Used only for tests RN
    protected open fun runStubProcessing() { }
}
