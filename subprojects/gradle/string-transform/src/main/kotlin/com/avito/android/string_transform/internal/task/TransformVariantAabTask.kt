package com.avito.android.string_transform.internal.task

import com.avito.android.Result
import com.avito.android.string_transform.internal.execution.runTransform
import com.avito.android.string_transform.internal.report.TransformReport
import com.avito.android.string_transform.internal.report.TransformReportJsonWriter
import com.avito.android.string_transform.internal.report.TransformReportRecorder
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

internal abstract class TransformVariantAabTask : DefaultTask() {

    @get:Input
    abstract val modulePath: Property<String>

    @get:Input
    abstract val pipelineName: Property<String>

    @get:Input
    abstract val variantName: Property<String>

    @get:Input
    abstract val totalRuleCount: Property<Int>

    @get:Input
    abstract val exactRuleCount: Property<Int>

    @get:Input
    abstract val caseExpandedRuleCount: Property<Int>

    @get:Input
    abstract val configurationWarnings: ListProperty<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputAabFile: RegularFileProperty

    @get:OutputFile
    abstract val reportFile: RegularFileProperty

    @TaskAction
    fun transform() {
        val recorder = TransformReportRecorder(
            modulePath = modulePath.get(),
            pipelineName = pipelineName.get(),
            variantName = variantName.get(),
            rules = TransformReport.Rules(
                totalRules = totalRuleCount.get(),
                declarationCounts = TransformReport.DeclarationCounts(
                    exact = exactRuleCount.get(),
                    caseExpanded = caseExpandedRuleCount.get(),
                )
            ),
            configurationWarnings = configurationWarnings.get(),
        )

        val branchResult = runAabTransform(recorder)
        val reportResult = TransformReportJsonWriter.write(
            report = recorder.build(),
            outputFile = reportFile.get().asFile,
        )

        branchResult.combine(reportResult) { _, _ -> }.getOrThrow()
    }

    private fun runAabTransform(recorder: TransformReportRecorder): Result<Unit> {
        val inputAab = inputAabFile.get().asFile

        return runTransform(recorder) {
            step("variant-aab-artifact-observation") {
                observeInputAab(inputAab)
            }
            step("input-aab-validation") {
                validateInputAab(inputAab)
            }
            Unit
        }
    }

    private fun observeInputAab(inputAab: File): Result<Unit> = Result.tryCatch {
        check(inputAab.exists()) {
            "Variant bundle artifact file does not exist: ${inputAab.path}"
        }
        check(inputAab.isFile) {
            "Variant bundle artifact path is not a file: ${inputAab.path}"
        }
    }

    private fun validateInputAab(inputAab: File): Result<Unit> = Result.tryCatch {
        require(inputAab.extension == "aab") {
            "Observed bundle artifact is not a publishable AAB candidate: ${inputAab.path}"
        }
    }
}
