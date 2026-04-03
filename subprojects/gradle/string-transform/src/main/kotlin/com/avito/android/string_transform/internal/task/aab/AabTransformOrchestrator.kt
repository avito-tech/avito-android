package com.avito.android.string_transform.internal.task.aab

import com.avito.android.Result
import com.avito.android.string_transform.internal.execution.runTransform
import com.avito.android.string_transform.internal.report.TransformReportRecorder
import com.avito.android.string_transform.internal.rules.NormalizedRule
import com.avito.android.string_transform.internal.task.apk.OperationWarning
import com.avito.android.string_transform.internal.task.apk.WorkspaceContentTransformer
import com.avito.android.string_transform.internal.task.apk.WorkspacePathRenamer
import org.gradle.api.file.RegularFileProperty
import java.io.File

internal class AabTransformOrchestrator(
    private val bundleArchiver: AabBundleArchiver,
    private val fileClassifier: AabWorkspaceFileClassifier,
    private val resourcesPbTransformer: AabResourcesPbTransformer,
    private val protobufXmlTransformer: AabProtobufXmlTransformer,
    private val dexTransformer: AabDexTransformer,
    private val contentTransformer: WorkspaceContentTransformer,
    private val pathRenamer: WorkspacePathRenamer,
    private val metadataCleaner: BundleMetadataCleaner,
    private val aabPublisher: AabOutputPublisher,
) {

    fun execute(
        recorder: TransformReportRecorder,
        inputAabFile: RegularFileProperty,
        localStateRoot: File,
        publishedAab: File,
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
            val rebuiltAab = localStateRoot.resolve("rebuilt-unsigned.aab")
            val coverage = AabProcessingCoverage(workspace)

            val inputAab = step("variant-aab-artifact-observation") {
                observeInputAab(inputAabFile)
            }
            step("input-aab-validation") {
                validateInputAab(inputAab)
            }
            step("bundle-unpack") {
                if (localStateRoot.exists()) {
                    localStateRoot.deleteRecursively()
                }
                localStateRoot.mkdirs()
                bundleArchiver.unpackToWorkspace(inputAab, workspace)
            }

            val discoveredFiles = discoverWorkspaceFiles(workspace)

            step("resources-pb-transform") {
                transformDiscoveredFiles(
                    files = discoveredFiles.resourcesPbFiles.asSequence(),
                    coverage = coverage,
                ) { file ->
                    resourcesPbTransformer.transform(file, rules)
                }
            }
            step("protobuf-xml-transform") {
                transformDiscoveredFiles(
                    files = discoveredFiles.protobufXmlFiles.asSequence(),
                    coverage = coverage,
                ) { file ->
                    protobufXmlTransformer.transform(file, rules)
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

            step("rename") {
                pathRenamer.rename(workspace, rules)
                    .onSuccess { warnings -> recordWarnings("rename", warnings) }
            }
            step("metadata-cleanup") {
                metadataCleaner.clean(workspace)
            }
            step("bundle-repack") {
                if (rebuiltAab.exists()) {
                    rebuiltAab.delete()
                }
                bundleArchiver.packFromWorkspace(workspace, rebuiltAab)
            }
            step("output-publication") {
                aabPublisher.publish(rebuiltAab, publishedAab)
            }
            Unit
        }
    }

    private fun observeInputAab(inputAabFile: RegularFileProperty): Result<File> = Result.tryCatch {
        val inputAab = requireNotNull(inputAabFile.orNull?.asFile) {
            "Variant bundle artifact file is not configured"
        }
        check(inputAab.exists()) {
            "Variant bundle artifact file does not exist: ${inputAab.path}"
        }
        check(inputAab.isFile) {
            "Variant bundle artifact path is not a file: ${inputAab.path}"
        }
        inputAab
    }

    private fun validateInputAab(inputAab: File): Result<Unit> = Result.tryCatch {
        require(inputAab.extension == "aab") {
            "Observed bundle artifact is not a publishable AAB candidate: ${inputAab.path}"
        }
    }

    private fun transformDiscoveredFiles(
        files: Sequence<File>,
        coverage: AabProcessingCoverage,
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
        coverage: AabProcessingCoverage,
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
        val resourcesPbFiles = mutableListOf<File>()
        val protobufXmlFiles = mutableListOf<File>()
        val dexFiles = mutableListOf<File>()
        val metadataFiles = mutableListOf<File>()
        val unsupportedBinaryFiles = mutableListOf<File>()

        workspaceDirectory.walkTopDown()
            .filter(File::isFile)
            .forEach { file ->
                when (fileClassifier.classify(workspaceDirectory, file)) {
                    AabWorkspaceFileClassifier.ArtifactClass.RESOURCES_PB -> resourcesPbFiles += file
                    AabWorkspaceFileClassifier.ArtifactClass.PROTOBUF_XML -> protobufXmlFiles += file
                    AabWorkspaceFileClassifier.ArtifactClass.DEX -> dexFiles += file
                    AabWorkspaceFileClassifier.ArtifactClass.METADATA -> metadataFiles += file
                    AabWorkspaceFileClassifier.ArtifactClass.UNSUPPORTED_BINARY -> unsupportedBinaryFiles += file
                    AabWorkspaceFileClassifier.ArtifactClass.RESIDUAL -> Unit
                }
            }

        return DiscoveredWorkspaceFiles(
            resourcesPbFiles = resourcesPbFiles,
            protobufXmlFiles = protobufXmlFiles,
            dexFiles = dexFiles,
            metadataFiles = metadataFiles,
            unsupportedBinaryFiles = unsupportedBinaryFiles,
        )
    }

    private data class DiscoveredWorkspaceFiles(
        val resourcesPbFiles: List<File>,
        val protobufXmlFiles: List<File>,
        val dexFiles: List<File>,
        val metadataFiles: List<File>,
        val unsupportedBinaryFiles: List<File>,
    )
}
