package com.avito.android.string_transform.internal.task

import com.avito.android.Result
import com.avito.android.string_transform.internal.report.TransformReport
import com.avito.android.string_transform.internal.report.TransformReportJsonWriter
import com.avito.android.string_transform.internal.report.TransformReportRecorder
import com.avito.android.string_transform.internal.rules.NormalizedRule
import com.avito.android.string_transform.internal.task.aab.AabBundleArchiver
import com.avito.android.string_transform.internal.task.aab.AabDexTransformer
import com.avito.android.string_transform.internal.task.aab.AabOutputPublisher
import com.avito.android.string_transform.internal.task.aab.AabProtobufXmlTransformer
import com.avito.android.string_transform.internal.task.aab.AabResourcesPbTransformer
import com.avito.android.string_transform.internal.task.aab.AabTransformOrchestrator
import com.avito.android.string_transform.internal.task.aab.AabWorkspaceFileClassifier
import com.avito.android.string_transform.internal.task.aab.BundleMetadataCleaner
import com.avito.android.string_transform.internal.task.apk.WorkspaceContentTransformer
import com.avito.android.string_transform.internal.task.apk.WorkspacePathRenamer
import com.avito.android.string_transform.internal.task.apk.ZeroByteTextFileDetector
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

    @get:Input
    abstract val rules: ListProperty<NormalizedRule>

    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputAabFile: RegularFileProperty

    @get:LocalState
    abstract val localStateDirectory: DirectoryProperty

    @get:OutputFile
    abstract val outputAabFile: RegularFileProperty

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
        return AabTransformOrchestrator(
            bundleArchiver = AabBundleArchiver(),
            fileClassifier = AabWorkspaceFileClassifier(),
            resourcesPbTransformer = AabResourcesPbTransformer(),
            protobufXmlTransformer = AabProtobufXmlTransformer(),
            dexTransformer = AabDexTransformer(),
            contentTransformer = WorkspaceContentTransformer(ZeroByteTextFileDetector()),
            pathRenamer = WorkspacePathRenamer(),
            metadataCleaner = BundleMetadataCleaner(),
            aabPublisher = AabOutputPublisher(),
        ).execute(
            recorder = recorder,
            inputAabFile = inputAabFile,
            localStateRoot = localStateDirectory.get().asFile,
            publishedAab = outputAabFile.get().asFile,
            rules = rules.get(),
        )
    }
}
