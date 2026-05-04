package com.avito.android.string_transform.task

import com.avito.android.Result
import com.avito.android.string_transform.internal.execution.runTransform
import com.avito.android.string_transform.internal.report.TransformReport
import com.avito.android.string_transform.internal.report.TransformReportJsonWriter
import com.avito.android.string_transform.internal.report.TransformReportRecorder
import com.avito.android.string_transform.internal.rules.NormalizedRule
import com.avito.android.string_transform.internal.task.OutputPublisher
import com.avito.android.string_transform.internal.task.VariantApkInputResolver
import com.avito.android.string_transform.internal.task.apk.ApktoolRunner
import com.avito.android.string_transform.internal.task.apk.OperationWarning
import com.avito.android.string_transform.internal.task.apk.WorkspaceContentTransformer
import com.avito.android.string_transform.internal.task.apk.WorkspacePathRenamer
import com.avito.android.string_transform.internal.task.apk.ZeroByteTextFileDetector
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.LocalState
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.toolchain.JavaLauncher

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

    @get:Classpath
    internal abstract val apktoolClasspath: ConfigurableFileCollection

    @get:Nested
    internal abstract val javaLauncher: Property<JavaLauncher>

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
        val transformRules = rules.get()

        fun recordWarnings(
            phase: String,
            warnings: List<OperationWarning>,
        ) {
            warnings.forEach { warning ->
                recorder.addWarning(
                    message = warning.message,
                    affectedPhase = phase,
                    affectedPath = warning.affectedPath,
                )
            }
        }

        return runTransform(recorder) {
            step("variant-apk-outputs-observation") {
                apkInputResolver.observe(inputDirectory)
            }
            val inputApk = step("input-apk-resolution") {
                apkInputResolver.resolveSingle(inputDirectory)
            }
            val apktoolRunner = ApktoolRunner(
                apktoolJar = apktoolClasspath.singleFile,
                javaExecutable = javaLauncher.get().executablePath.asFile.absolutePath,
            )
            val contentTransformer = WorkspaceContentTransformer(ZeroByteTextFileDetector())
            val pathRenamer = WorkspacePathRenamer()
            val outputPublisher = OutputPublisher()
            val localStateRoot = localStateDirectory.get().asFile
            val workspace = localStateRoot.resolve("decoded")
            val rebuiltApk = localStateRoot.resolve("rebuilt-unsigned.apk")
            val publishedApk = outputApkFile.get().asFile

            step("apktool-decode") {
                if (localStateRoot.exists()) {
                    localStateRoot.deleteRecursively()
                }
                localStateRoot.mkdirs()
                apktoolRunner.decode(inputApk, workspace)
            }
            step("content-transform") {
                contentTransformer.transform(workspace, transformRules)
                    .onSuccess { warnings -> recordWarnings("content-transform", warnings) }
            }
            step("rename") {
                pathRenamer.rename(workspace, transformRules)
                    .onSuccess { warnings -> recordWarnings("rename", warnings) }
            }
            step("apktool-build") {
                if (rebuiltApk.exists()) {
                    rebuiltApk.delete()
                }
                apktoolRunner.build(workspace, rebuiltApk)
            }
            step("output-publication") {
                outputPublisher.publish(rebuiltApk, publishedApk)
            }
            Unit
        }
    }
}
