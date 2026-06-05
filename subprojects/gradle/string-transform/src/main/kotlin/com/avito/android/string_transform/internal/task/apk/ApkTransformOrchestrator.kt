package com.avito.android.string_transform.internal.task.apk

import com.avito.android.Result
import com.avito.android.string_transform.internal.execution.runTransform
import com.avito.android.string_transform.internal.report.TransformReportRecorder
import com.avito.android.string_transform.internal.rules.NormalizedRule
import com.avito.android.string_transform.internal.task.OutputPublisher
import com.avito.android.string_transform.internal.task.common.BinaryArchiver
import com.avito.android.string_transform.internal.task.common.DexTransformer
import com.avito.android.string_transform.internal.task.common.KotlinModuleTransformer
import com.avito.android.string_transform.internal.task.common.MetadataCleaner
import com.avito.android.string_transform.internal.task.common.ProcessingCoverage
import java.io.File

internal class ApkTransformOrchestrator(
    private val apkArchiver: BinaryArchiver,
    private val fileClassifier: ApkWorkspaceFileClassifier,
    private val arscTransformer: BinaryArscTransformer,
    private val binaryAxmlTransformer: BinaryAxmlTransformer,
    private val dexTransformer: DexTransformer,
    private val kotlinModuleTransformer: KotlinModuleTransformer,
    private val contentTransformer: WorkspaceContentTransformer,
    private val pathRenamer: WorkspacePathRenamer,
    private val metadataCleaner: MetadataCleaner,
    private val outputPublisher: OutputPublisher,
) {

    fun execute(
        recorder: TransformReportRecorder,
        inputApk: File,
        localStateRoot: File,
        publishedApk: File,
        rules: List<NormalizedRule>,
    ): Result<Unit> {
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
            val workspace = localStateRoot.resolve("unpacked")
            val rebuiltApk = localStateRoot.resolve("rebuilt-unsigned.apk")
            val coverage = ProcessingCoverage(workspace)

            step("variant-apk-artifact-observation") {
                observeInputApk(inputApk)
            }
            step("input-apk-validation") {
                validateInputApk(inputApk)
            }
            var compressionManifest = step("apk-unpack") {
                if (localStateRoot.exists()) {
                    localStateRoot.deleteRecursively()
                }
                localStateRoot.mkdirs()
                apkArchiver.unpackToWorkspace(inputApk, workspace)
            }

            val discoveredFiles = discoverWorkspaceFiles(workspace)

            step("arsc-transform") {
                transformDiscoveredFiles(
                    files = discoveredFiles.arscFiles.asSequence(),
                    coverage = coverage,
                ) { file ->
                    arscTransformer.transform(file, rules)
                }
            }
            step("binary-axml-transform") {
                transformDiscoveredFiles(
                    files = discoveredFiles.binaryAxmlFiles.asSequence(),
                    coverage = coverage,
                ) { file ->
                    binaryAxmlTransformer.transform(file, rules)
                }
            }
            val dexWarnings = step("dex-transform") {
                transformDiscoveredDexFiles(
                    files = discoveredFiles.dexFiles.asSequence(),
                    workspaceDirectory = workspace,
                    coverage = coverage,
                    rules = rules,
                )
            }
            recordWarnings("dex-transform", dexWarnings)

            step("kotlin-module-transform") {
                transformDiscoveredFiles(
                    files = discoveredFiles.kotlinModuleFiles.asSequence(),
                    coverage = coverage,
                ) { file -> kotlinModuleTransformer.transform(file, rules) }
            }

            discoveredFiles.metadataFiles.forEach(coverage::markSkipped)
            discoveredFiles.unsupportedBinaryFiles.forEach(coverage::markSkipped)

            val unsupportedBinaryWarnings = step("residual-text-transform") {
                contentTransformer.transform(
                    workspaceDirectory = workspace,
                    files = workspace.walkTopDown().filter(File::isFile).filter(coverage::isResidualCandidate),
                    rules = rules,
                )
            }
            recordWarnings("residual-text-transform", unsupportedBinaryWarnings)

            val renameResult = step("rename") {
                pathRenamer.rename(workspace, rules)
            }
            recordWarnings("rename", renameResult.warnings)
            compressionManifest = compressionManifest.remapKeys(renameResult.pathMapping)

            step("metadata-cleanup") {
                metadataCleaner.clean(workspace)
            }
            step("apk-repack") {
                if (rebuiltApk.exists()) {
                    rebuiltApk.delete()
                }
                val finalRelPaths = workspace.walkTopDown()
                    .filter(File::isFile)
                    .map { it.relativeTo(workspace).invariantSeparatorsPath }
                    .toSet()
                val missingStoredWarnings =
                    compressionManifest.storedEntriesMissing(finalRelPaths).map { relPath ->
                        OperationWarning(
                            message = "Original STORED entry was dropped before repack " +
                                "and will not be emitted: $relPath",
                            affectedPath = relPath,
                        )
                    }
                recordWarnings("apk-repack", missingStoredWarnings)
                apkArchiver.packFromWorkspace(workspace, rebuiltApk, compressionManifest)
            }
            step("output-publication") {
                outputPublisher.publish(rebuiltApk, publishedApk)
            }
            Unit
        }
    }

    private fun observeInputApk(inputApk: File): Result<Unit> = Result.tryCatch {
        check(inputApk.exists()) {
            "Variant APK artifact file does not exist: ${inputApk.path}"
        }
        check(inputApk.isFile) {
            "Variant APK artifact path is not a file: ${inputApk.path}"
        }
    }

    private fun validateInputApk(inputApk: File): Result<Unit> = Result.tryCatch {
        require(inputApk.extension == "apk") {
            "Observed APK artifact is not a publishable APK candidate: ${inputApk.path}"
        }
    }

    private fun transformDiscoveredFiles(
        files: Sequence<File>,
        coverage: ProcessingCoverage,
        transform: (File) -> Result<Unit>,
    ): Result<Unit> {
        var result: Result<Unit> = Result.Success(Unit)

        files.forEach { file ->
            result = result.flatMap {
                transform(file).onSuccess {
                    coverage.markHandled(file)
                }
            }
        }

        return result
    }

    private fun transformDiscoveredDexFiles(
        files: Sequence<File>,
        workspaceDirectory: File,
        coverage: ProcessingCoverage,
        rules: List<NormalizedRule>,
    ): Result<List<OperationWarning>> {
        val warnings = mutableListOf<OperationWarning>()
        var result: Result<Unit> = Result.Success(Unit)

        files.forEach { file ->
            result = result.flatMap {
                dexTransformer.transform(
                    inputFile = file,
                    rules = rules,
                    affectedPath = file.relativeTo(workspaceDirectory).invariantSeparatorsPath,
                ).onSuccess { fileWarnings ->
                    coverage.markHandled(file)
                    warnings += fileWarnings
                }.map { Unit }
            }
        }

        return result.map { warnings.toList() }
    }

    private fun discoverWorkspaceFiles(workspaceDirectory: File): DiscoveredWorkspaceFiles {
        val arscFiles = mutableListOf<File>()
        val binaryAxmlFiles = mutableListOf<File>()
        val dexFiles = mutableListOf<File>()
        val kotlinModuleFiles = mutableListOf<File>()
        val metadataFiles = mutableListOf<File>()
        val unsupportedBinaryFiles = mutableListOf<File>()

        workspaceDirectory.walkTopDown()
            .filter(File::isFile)
            .forEach { file ->
                when (fileClassifier.classify(workspaceDirectory, file)) {
                    ApkWorkspaceFileClassifier.ArtifactClass.RESOURCES_ARSC -> arscFiles += file
                    ApkWorkspaceFileClassifier.ArtifactClass.BINARY_AXML -> binaryAxmlFiles += file
                    ApkWorkspaceFileClassifier.ArtifactClass.DEX -> dexFiles += file
                    ApkWorkspaceFileClassifier.ArtifactClass.KOTLIN_MODULE -> kotlinModuleFiles += file
                    ApkWorkspaceFileClassifier.ArtifactClass.METADATA -> metadataFiles += file
                    ApkWorkspaceFileClassifier.ArtifactClass.UNSUPPORTED_BINARY -> unsupportedBinaryFiles += file
                    ApkWorkspaceFileClassifier.ArtifactClass.RESIDUAL -> Unit
                }
            }

        return DiscoveredWorkspaceFiles(
            arscFiles = arscFiles,
            binaryAxmlFiles = binaryAxmlFiles,
            dexFiles = dexFiles,
            kotlinModuleFiles = kotlinModuleFiles,
            metadataFiles = metadataFiles,
            unsupportedBinaryFiles = unsupportedBinaryFiles,
        )
    }

    private data class DiscoveredWorkspaceFiles(
        val arscFiles: List<File>,
        val binaryAxmlFiles: List<File>,
        val dexFiles: List<File>,
        val kotlinModuleFiles: List<File>,
        val metadataFiles: List<File>,
        val unsupportedBinaryFiles: List<File>,
    )
}
