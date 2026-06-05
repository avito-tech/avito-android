package com.avito.android.string_transform.internal.task.aab

import com.avito.android.string_transform.createDexBytes
import com.avito.android.string_transform.createKotlinModuleBytes
import com.avito.android.string_transform.createProtobufXmlBytes
import com.avito.android.string_transform.createResourcesPbBytes
import com.avito.android.string_transform.createZip
import com.avito.android.string_transform.internal.report.ReportDiagnosticSeverity
import com.avito.android.string_transform.internal.report.TransformReport
import com.avito.android.string_transform.internal.report.TransformReportRecorder
import com.avito.android.string_transform.internal.task.OutputPublisher
import com.avito.android.string_transform.internal.task.apk.WorkspaceContentTransformer
import com.avito.android.string_transform.internal.task.apk.WorkspacePathRenamer
import com.avito.android.string_transform.internal.task.apk.ZeroByteTextFileDetector
import com.avito.android.string_transform.internal.task.common.BinaryArchiver
import com.avito.android.string_transform.internal.task.common.DexTransformer
import com.avito.android.string_transform.internal.task.common.KotlinModuleTransformer
import com.avito.android.string_transform.internal.task.common.MetadataCleaner
import com.google.common.truth.Truth.assertThat
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

internal class AabTransformOrchestratorTest {

    @Test
    fun `execute - preserves STORED compression method through round-trip`(
        @TempDir dir: File,
    ) {
        val inputAab = dir.resolve("input/variant.aab")
        createAabWithStoredEntry(
            archive = inputAab,
            storedPaths = setOf("base/lib/arm64-v8a/libnative.so"),
        )

        val recorder = newRecorder()
        val outputAab = dir.resolve("output/variant.aab")
        val localStateRoot = dir.resolve("local-state")
        val inputAabProperty = newFileProperty(dir, inputAab)

        newOrchestrator().execute(
            recorder = recorder,
            inputAabFile = inputAabProperty,
            localStateRoot = localStateRoot,
            publishedAab = outputAab,
            rules = emptyList(),
        ).getOrThrow()

        ZipFile(outputAab).use { zip ->
            val soEntry = zip.getEntry("base/lib/arm64-v8a/libnative.so")
            assertThat(soEntry).isNotNull()
            assertThat(soEntry.method).isEqualTo(ZipEntry.STORED)
        }

        val report = recorder.build()
        val repackWarnings = report.diagnostics.filter {
            it.severity == ReportDiagnosticSeverity.WARNING && it.affectedPhase == "bundle-repack"
        }
        assertThat(repackWarnings).isEmpty()
    }

    @Test
    fun `execute - emits warning when STORED entry is dropped before repack`(
        @TempDir dir: File,
    ) {
        val inputAab = dir.resolve("input/variant.aab")
        createAabWithStoredEntry(
            archive = inputAab,
            storedPaths = setOf("META-INF/BNDLTOOL.SF"),
        )

        val recorder = newRecorder()
        val outputAab = dir.resolve("output/variant.aab")
        val inputAabProperty = newFileProperty(dir, inputAab)

        newOrchestrator().execute(
            recorder = recorder,
            inputAabFile = inputAabProperty,
            localStateRoot = dir.resolve("local-state"),
            publishedAab = outputAab,
            rules = emptyList(),
        ).getOrThrow()

        val report = recorder.build()
        val repackWarnings = report.diagnostics.filter {
            it.severity == ReportDiagnosticSeverity.WARNING && it.affectedPhase == "bundle-repack"
        }
        assertThat(repackWarnings).hasSize(1)
        assertThat(repackWarnings[0].message).contains("Original STORED entry was dropped before repack")
        assertThat(repackWarnings[0].affectedPath).isEqualTo("META-INF/BNDLTOOL.SF")
    }

    private fun createAabWithStoredEntry(
        archive: File,
        storedPaths: Set<String> = emptySet(),
    ) {
        val opaqueProtobufPayload = byteArrayOf(0x0A, 0x06, 0x6F, 0x70, 0x61, 0x71, 0x75, 0x65)
        createZip(
            archive = archive,
            entries = mapOf(
                "BundleConfig.pb" to opaqueProtobufPayload,
                "base/resources.pb" to createResourcesPbBytes(),
                "base/manifest/AndroidManifest.xml" to createProtobufXmlBytes(),
                "base/dex/classes.dex" to createDexBytes(),
                "base/root/META-INF/samplevalue_module.kotlin_module" to createKotlinModuleBytes(),
                "base/lib/arm64-v8a/libnative.so" to byteArrayOf(0x7F, 0x45, 0x4C, 0x46),
                "META-INF/BNDLTOOL.SF" to "signature".toByteArray(StandardCharsets.UTF_8),
                "META-INF/services/demo.Service" to "implementation".toByteArray(StandardCharsets.UTF_8),
            ),
            storedPaths = storedPaths,
        )
    }

    private fun newFileProperty(projectDir: File, file: File) =
        ProjectBuilder.builder()
            .withProjectDir(projectDir.resolve("gradle-project").apply { mkdirs() })
            .build()
            .objects
            .fileProperty()
            .apply { set(file) }

    private fun newOrchestrator(): AabTransformOrchestrator {
        return AabTransformOrchestrator(
            bundleArchiver = BinaryArchiver(),
            fileClassifier = AabWorkspaceFileClassifier(),
            resourcesPbTransformer = AabResourcesPbTransformer(),
            protobufXmlTransformer = AabProtobufXmlTransformer(),
            dexTransformer = DexTransformer(),
            kotlinModuleTransformer = KotlinModuleTransformer(),
            contentTransformer = WorkspaceContentTransformer(ZeroByteTextFileDetector()),
            pathRenamer = WorkspacePathRenamer(),
            metadataCleaner = MetadataCleaner(),
            outputPublisher = OutputPublisher(),
        )
    }

    private fun newRecorder(): TransformReportRecorder {
        return TransformReportRecorder(
            modulePath = ":sample",
            pipelineName = "sample",
            variantName = "release",
            rules = TransformReport.Rules(
                totalRules = 1,
                declarationCounts = TransformReport.DeclarationCounts(exact = 1, caseExpanded = 0),
            ),
            configurationWarnings = emptyList(),
        )
    }
}
