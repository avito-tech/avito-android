package com.avito.android.string_transform.task

import com.avito.android.Result
import com.avito.android.string_transform.internal.execution.runTransform
import com.avito.android.string_transform.internal.report.TransformReport
import com.avito.android.string_transform.internal.report.TransformReportJsonWriter
import com.avito.android.string_transform.internal.report.TransformReportRecorder
import com.avito.android.string_transform.internal.rules.NormalizedRule
import com.avito.android.string_transform.internal.task.OutputPublisher
import com.avito.android.string_transform.internal.task.VariantApkInputResolver
import com.avito.android.string_transform.internal.task.apk.ApkTransformOrchestrator
import com.avito.android.string_transform.internal.task.apk.ApkWorkspaceFileClassifier
import com.avito.android.string_transform.internal.task.apk.BinaryArscTransformer
import com.avito.android.string_transform.internal.task.apk.BinaryAxmlTransformer
import com.avito.android.string_transform.internal.task.apk.WorkspaceContentTransformer
import com.avito.android.string_transform.internal.task.apk.WorkspacePathRenamer
import com.avito.android.string_transform.internal.task.apk.ZeroByteTextFileDetector
import com.avito.android.string_transform.internal.task.common.BinaryArchiver
import com.avito.android.string_transform.internal.task.common.DexTransformer
import com.avito.android.string_transform.internal.task.common.KotlinModuleTransformer
import com.avito.android.string_transform.internal.task.common.MetadataCleaner
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.LocalState
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

public abstract class TransformVariantApkTask : DefaultTask() {

    @get:Input
    public abstract val pipelineName: Property<String>

    @get:Input
    public abstract val variantName: Property<String>

    @get:OutputFile
    public abstract val outputApkFile: RegularFileProperty

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

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    internal abstract val apkDirectory: DirectoryProperty

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

        val branchResult = runApkTransform(recorder)
        val reportResult = TransformReportJsonWriter.write(
            report = recorder.build(),
            outputFile = reportFile.get().asFile,
        )

        branchResult.combine(reportResult) { _, _ -> }.getOrThrow()
    }

    private fun runApkTransform(recorder: TransformReportRecorder): Result<Unit> {
        val inputDirectory = apkDirectory.get().asFile
        val apkInputResolver = VariantApkInputResolver()

        var resolvedApk: File? = null
        val resolutionResult = runTransform(recorder) {
            step("variant-apk-outputs-observation") {
                apkInputResolver.observe(inputDirectory)
            }
            resolvedApk = step("input-apk-resolution") {
                apkInputResolver.resolveSingle(inputDirectory)
            }
        }

        return resolutionResult.flatMap {
            ApkTransformOrchestrator(
                apkArchiver = BinaryArchiver(),
                fileClassifier = ApkWorkspaceFileClassifier(),
                arscTransformer = BinaryArscTransformer(),
                binaryAxmlTransformer = BinaryAxmlTransformer(),
                dexTransformer = DexTransformer(),
                kotlinModuleTransformer = KotlinModuleTransformer(),
                contentTransformer = WorkspaceContentTransformer(ZeroByteTextFileDetector()),
                pathRenamer = WorkspacePathRenamer(),
                metadataCleaner = MetadataCleaner(),
                outputPublisher = OutputPublisher(),
            ).execute(
                recorder = recorder,
                inputApk = checkNotNull(resolvedApk),
                localStateRoot = localStateDirectory.get().asFile,
                publishedApk = outputApkFile.get().asFile,
                rules = rules.get(),
            )
        }
    }
}
