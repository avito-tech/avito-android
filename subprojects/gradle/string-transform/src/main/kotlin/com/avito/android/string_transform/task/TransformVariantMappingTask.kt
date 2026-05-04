package com.avito.android.string_transform.task

import com.avito.android.Result
import com.avito.android.string_transform.internal.execution.runTransform
import com.avito.android.string_transform.internal.report.TransformReport
import com.avito.android.string_transform.internal.report.TransformReportJsonWriter
import com.avito.android.string_transform.internal.report.TransformReportRecorder
import com.avito.android.string_transform.internal.rules.NormalizedRule
import com.avito.android.string_transform.internal.task.OutputPublisher
import com.avito.android.string_transform.internal.task.mapping.MappingStructuralSanityValidator
import com.avito.android.string_transform.internal.task.mapping.MappingTextTransformer
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.LocalState
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

public abstract class TransformVariantMappingTask : DefaultTask() {

    @get:Input
    public abstract val pipelineName: Property<String>

    @get:Input
    public abstract val variantName: Property<String>

    @get:OutputFile
    public abstract val outputMappingFile: RegularFileProperty

    @get:OutputFile
    public abstract val reportFile: RegularFileProperty

    @get:Input
    internal abstract val modulePath: Property<String>

    @get:Input
    internal abstract val totalRuleCount: Property<Int>

    @get:Input
    internal abstract val exactRuleCount: Property<Int>

    @get:Input
    internal abstract val caseExpandedRuleCount: Property<Int>

    @get:Input
    internal abstract val configurationWarnings: ListProperty<String>

    @get:Input
    internal abstract val rules: ListProperty<NormalizedRule>

    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    internal abstract val inputMappingFile: RegularFileProperty

    @get:LocalState
    internal abstract val localStateDirectory: DirectoryProperty

    @TaskAction
    public fun transform() {
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

        val branchResult = runMappingTransform(recorder)
        val reportResult = TransformReportJsonWriter.write(
            report = recorder.build(),
            outputFile = reportFile.get().asFile,
        )

        branchResult.combine(reportResult) { _, _ -> }.getOrThrow()
    }

    private fun runMappingTransform(recorder: TransformReportRecorder): Result<Unit> {
        val transformRules = rules.get()
        val validator = MappingStructuralSanityValidator()
        val outputPublisher = OutputPublisher()
        val textTransformer = MappingTextTransformer()

        return runTransform(recorder) {
            val inputMapping = step("variant-mapping-artifact-observation") {
                observeInputMapping()
            }
            step("input-mapping-validation") {
                validateInputMapping(inputMapping)
            }

            val localStateRoot = localStateDirectory.get().asFile
            val transformedMapping = localStateRoot.resolve("transformed-mapping.txt")
            val publishedMapping = outputMappingFile.get().asFile

            step("mapping-text-transform") {
                if (localStateRoot.exists()) {
                    localStateRoot.deleteRecursively()
                }
                localStateRoot.mkdirs()
                textTransformer.transform(
                    inputMapping = inputMapping,
                    outputMapping = transformedMapping,
                    rules = transformRules,
                )
            }
            step("mapping-structural-sanity-validation") {
                validator.validate(transformedMapping)
            }
            step("output-publication") {
                outputPublisher.publish(transformedMapping, publishedMapping)
            }
            Unit
        }
    }

    private fun observeInputMapping(): Result<File> = Result.tryCatch {
        val inputMapping = inputMappingFile.orNull?.asFile
            ?: error("Variant mapping artifact is not available")

        check(inputMapping.exists()) {
            "Variant mapping artifact file does not exist: ${inputMapping.path}"
        }

        inputMapping
    }

    private fun validateInputMapping(
        inputMapping: File,
    ): Result<Unit> = Result.tryCatch {
        check(inputMapping.isFile) {
            "Observed mapping artifact is not a file: ${inputMapping.path}"
        }
    }
}
