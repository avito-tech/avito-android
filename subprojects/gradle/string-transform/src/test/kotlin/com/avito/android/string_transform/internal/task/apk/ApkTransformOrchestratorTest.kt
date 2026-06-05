package com.avito.android.string_transform.internal.task.apk

import com.avito.android.isFailure
import com.avito.android.string_transform.createApkFixture
import com.avito.android.string_transform.createArscBytes
import com.avito.android.string_transform.createBinaryAxmlBytes
import com.avito.android.string_transform.createDexBytes
import com.avito.android.string_transform.internal.report.ReportDiagnosticSeverity
import com.avito.android.string_transform.internal.report.TransformReport
import com.avito.android.string_transform.internal.report.TransformReportRecorder
import com.avito.android.string_transform.internal.rules.NormalizedRule
import com.avito.android.string_transform.internal.task.OutputPublisher
import com.avito.android.string_transform.internal.task.common.BinaryArchiver
import com.avito.android.string_transform.internal.task.common.DexTransformer
import com.avito.android.string_transform.internal.task.common.KotlinModuleTransformer
import com.avito.android.string_transform.internal.task.common.MetadataCleaner
import com.avito.android.string_transform.readEntryBytes
import com.avito.android.string_transform.readEntryText
import com.google.common.truth.Truth.assertThat
import org.jf.dexlib2.DexFileFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

internal class ApkTransformOrchestratorTest {

    @Test
    fun `execute - rewrites strings across all artifact classes and strips signing files`(
        @TempDir dir: File,
    ) {
        val inputApk = dir.resolve("input/variant.apk")
        inputApk.parentFile.mkdirs()
        inputApk.writeBytes(
            createApkFixture(
                entries = mapOf(
                    "AndroidManifest.xml" to createBinaryAxmlBytes(
                        strings = listOf("samplevalue"),
                        rootElement = "manifest",
                    ),
                    "classes.dex" to createDexBytes(token = "samplevalue"),
                    "resources.arsc" to createArscBytes(
                        strings = listOf("samplevalue", "untouched"),
                        packageTypeStrings = listOf("string"),
                        packageKeyStrings = listOf("samplevalue_title", "untouched_name"),
                    ),
                    "assets/data.txt" to "samplevalue inside an asset\n".toByteArray(),
                    "META-INF/CERT.RSA" to byteArrayOf(0x01, 0x02, 0x03),
                    "META-INF/services/com.example.samplevalue.Service" to
                        "com.example.samplevalue.ServiceImpl\n".toByteArray(),
                ),
            ),
        )

        val recorder = newRecorder()
        val outputApk = dir.resolve("output/variant.apk")
        val localStateRoot = dir.resolve("local-state")

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
            inputApk = inputApk,
            localStateRoot = localStateRoot,
            publishedApk = outputApk,
            rules = listOf(NormalizedRule(from = "samplevalue", to = "changedvalue")),
        ).getOrThrow()

        assertThat(outputApk.exists()).isTrue()

        ZipFile(outputApk).use { zip ->
            val manifestBytes = zip.readEntryBytes("AndroidManifest.xml")
            val manifestStrings = StringPoolCodec
                .parse(manifestBytes, chunkOffset = AXML_HEADER_SIZE)
                .pool
                .strings
            assertThat(manifestStrings).contains("changedvalue")
            assertThat(manifestStrings).doesNotContain("samplevalue")

            val arscBytes = zip.readEntryBytes("resources.arsc")
            val arscPool = StringPoolCodec.parse(arscBytes, chunkOffset = ARSC_HEADER_SIZE)
            assertThat(arscPool.pool.strings).containsExactly("changedvalue", "untouched").inOrder()

            val packageOffset = ARSC_HEADER_SIZE + arscPool.chunkSize
            val packageBuffer = ByteBuffer.wrap(arscBytes).order(ByteOrder.LITTLE_ENDIAN)
            val typeStringsOffset = packageBuffer.getInt(packageOffset + 268)
            val keyStringsOffset = packageBuffer.getInt(packageOffset + 276)
            val packageKeyStrings = StringPoolCodec
                .parse(arscBytes, packageOffset + keyStringsOffset)
                .pool
                .strings
            assertThat(packageKeyStrings)
                .containsExactly("changedvalue_title", "untouched_name")
                .inOrder()
            val packageTypeStrings = StringPoolCodec
                .parse(arscBytes, packageOffset + typeStringsOffset)
                .pool
                .strings
            assertThat(packageTypeStrings).containsExactly("string").inOrder()

            val dexFile = loadDex(dir, zip.readEntryBytes("classes.dex"))
            val dexClassType = dexFile.classes.single().type
            assertThat(dexClassType).contains("changedvalue")
            assertThat(dexClassType).doesNotContain("samplevalue")

            assertThat(zip.readEntryText("assets/data.txt"))
                .isEqualTo("changedvalue inside an asset\n")

            assertThat(zip.getEntry("META-INF/CERT.RSA")).isNull()
            assertThat(zip.getEntry("META-INF/services/com.example.samplevalue.Service")).isNull()
            assertThat(zip.readEntryText("META-INF/services/com.example.changedvalue.Service"))
                .isEqualTo("com.example.changedvalue.ServiceImpl\n")
        }

        val report = recorder.build()
        val phaseNames = report.phases.map(TransformReport.Phase::name)
        assertThat(phaseNames).containsAtLeast(
            "variant-apk-artifact-observation",
            "input-apk-validation",
            "apk-unpack",
            "arsc-transform",
            "binary-axml-transform",
            "dex-transform",
            "kotlin-module-transform",
            "residual-text-transform",
            "rename",
            "metadata-cleanup",
            "apk-repack",
            "output-publication",
        ).inOrder()
    }

    @Test
    fun `execute - fails artifact observation - when input apk file does not exist`(
        @TempDir dir: File,
    ) {
        val orchestrator = newOrchestrator()
        val recorder = newRecorder()

        val result = orchestrator.execute(
            recorder = recorder,
            inputApk = dir.resolve("input/missing.apk"),
            localStateRoot = dir.resolve("local-state"),
            publishedApk = dir.resolve("output/variant.apk"),
            rules = emptyList(),
        )

        assertThat(result.isFailure()).isTrue()
        val phaseNames = recorder.build().phases.map(TransformReport.Phase::name)
        assertThat(phaseNames).containsExactly("variant-apk-artifact-observation")
    }

    @Test
    fun `execute - fails input validation - when artifact extension is not apk`(
        @TempDir dir: File,
    ) {
        val orchestrator = newOrchestrator()
        val recorder = newRecorder()
        val notAnApk = dir.resolve("input/variant.aab").apply {
            parentFile.mkdirs()
            writeBytes(byteArrayOf(0x01, 0x02))
        }

        val result = orchestrator.execute(
            recorder = recorder,
            inputApk = notAnApk,
            localStateRoot = dir.resolve("local-state"),
            publishedApk = dir.resolve("output/variant.apk"),
            rules = emptyList(),
        )

        assertThat(result.isFailure()).isTrue()
        val phaseNames = recorder.build().phases.map(TransformReport.Phase::name)
        assertThat(phaseNames).containsExactly(
            "variant-apk-artifact-observation",
            "input-apk-validation",
        ).inOrder()
    }

    @Test
    fun `execute - emits warning when STORED entry is dropped before repack`(
        @TempDir dir: File,
    ) {
        val inputApk = dir.resolve("input/variant.apk")
        inputApk.parentFile.mkdirs()
        inputApk.writeBytes(
            createApkFixture(
                entries = mapOf(
                    "resources.arsc" to createArscBytes(strings = listOf("test")),
                    "META-INF/CERT.RSA" to byteArrayOf(0x01, 0x02, 0x03),
                ),
                storedPaths = setOf("META-INF/CERT.RSA"),
            ),
        )

        val recorder = newRecorder()
        val outputApk = dir.resolve("output/variant.apk")

        newOrchestrator().execute(
            recorder = recorder,
            inputApk = inputApk,
            localStateRoot = dir.resolve("local-state"),
            publishedApk = outputApk,
            rules = emptyList(),
        ).getOrThrow()

        val report = recorder.build()
        val repackWarnings = report.diagnostics.filter {
            it.severity == ReportDiagnosticSeverity.WARNING && it.affectedPhase == "apk-repack"
        }
        assertThat(repackWarnings).hasSize(1)
        assertThat(repackWarnings[0].message).contains("Original STORED entry was dropped before repack")
        assertThat(repackWarnings[0].affectedPath).isEqualTo("META-INF/CERT.RSA")
    }

    @Test
    fun `execute - preserves STORED compression method through round-trip`(
        @TempDir dir: File,
    ) {
        val inputApk = dir.resolve("input/variant.apk")
        inputApk.parentFile.mkdirs()
        inputApk.writeBytes(
            createApkFixture(
                entries = mapOf(
                    "resources.arsc" to createArscBytes(strings = listOf("test")),
                    "lib/arm64-v8a/libnative.so" to byteArrayOf(0x7F, 0x45, 0x4C, 0x46),
                ),
                storedPaths = setOf("lib/arm64-v8a/libnative.so"),
            ),
        )

        val recorder = newRecorder()
        val outputApk = dir.resolve("output/variant.apk")

        newOrchestrator().execute(
            recorder = recorder,
            inputApk = inputApk,
            localStateRoot = dir.resolve("local-state"),
            publishedApk = outputApk,
            rules = emptyList(),
        ).getOrThrow()

        ZipFile(outputApk).use { zip ->
            val soEntry = zip.getEntry("lib/arm64-v8a/libnative.so")
            assertThat(soEntry).isNotNull()
            assertThat(soEntry.method).isEqualTo(ZipEntry.STORED)
        }

        val report = recorder.build()
        val repackWarnings = report.diagnostics.filter {
            it.severity == ReportDiagnosticSeverity.WARNING && it.affectedPhase == "apk-repack"
        }
        assertThat(repackWarnings).isEmpty()
    }

    @Test
    fun `execute - preserves STORED compression method when STORED entry is renamed`(
        @TempDir dir: File,
    ) {
        val inputApk = dir.resolve("input/variant.apk")
        inputApk.parentFile.mkdirs()
        inputApk.writeBytes(
            createApkFixture(
                entries = mapOf(
                    "resources.arsc" to createArscBytes(strings = listOf("samplevalue")),
                    "lib/arm64-v8a/libsamplevalue.so" to byteArrayOf(0x7F, 0x45, 0x4C, 0x46),
                ),
                storedPaths = setOf("lib/arm64-v8a/libsamplevalue.so"),
            ),
        )

        val recorder = newRecorder()
        val outputApk = dir.resolve("output/variant.apk")

        newOrchestrator().execute(
            recorder = recorder,
            inputApk = inputApk,
            localStateRoot = dir.resolve("local-state"),
            publishedApk = outputApk,
            rules = listOf(NormalizedRule(from = "samplevalue", to = "changedvalue")),
        ).getOrThrow()

        ZipFile(outputApk).use { zip ->
            val soEntry = zip.getEntry("lib/arm64-v8a/libchangedvalue.so")
            assertThat(soEntry).isNotNull()
            assertThat(soEntry.method).isEqualTo(ZipEntry.STORED)
            assertThat(zip.getEntry("lib/arm64-v8a/libsamplevalue.so")).isNull()
        }

        val report = recorder.build()
        val repackWarnings = report.diagnostics.filter {
            it.severity == ReportDiagnosticSeverity.WARNING && it.affectedPhase == "apk-repack"
        }
        assertThat(repackWarnings).isEmpty()
    }

    @Test
    fun `execute - deleted DEFLATED metadata does not trigger stored reconcile warning`(
        @TempDir dir: File,
    ) {
        val inputApk = dir.resolve("input/variant.apk")
        inputApk.parentFile.mkdirs()
        inputApk.writeBytes(
            createApkFixture(
                entries = mapOf(
                    "resources.arsc" to createArscBytes(strings = listOf("test")),
                    "META-INF/CERT.RSA" to byteArrayOf(0x01, 0x02, 0x03),
                    "BUNDLE-METADATA/com.android.tools.build.libraries/dependencies.pb" to
                        byteArrayOf(0x0A, 0x06),
                ),
            ),
        )

        val recorder = newRecorder()
        val outputApk = dir.resolve("output/variant.apk")

        newOrchestrator().execute(
            recorder = recorder,
            inputApk = inputApk,
            localStateRoot = dir.resolve("local-state"),
            publishedApk = outputApk,
            rules = emptyList(),
        ).getOrThrow()

        ZipFile(outputApk).use { zip ->
            assertThat(zip.getEntry("META-INF/CERT.RSA")).isNull()
            assertThat(zip.getEntry("BUNDLE-METADATA/com.android.tools.build.libraries/dependencies.pb"))
                .isNull()
        }

        val report = recorder.build()
        val repackWarnings = report.diagnostics.filter {
            it.severity == ReportDiagnosticSeverity.WARNING && it.affectedPhase == "apk-repack"
        }
        assertThat(repackWarnings).isEmpty()
    }

    private fun newOrchestrator(): ApkTransformOrchestrator {
        return ApkTransformOrchestrator(
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

    private fun loadDex(
        tempDirectory: File,
        bytes: ByteArray,
    ) = DexFileFactory.loadDexFile(
        tempDirectory.resolve("dex-read-back/classes.dex")
            .also { it.parentFile.mkdirs() }
            .apply { writeBytes(bytes) },
        null,
    )

    private companion object {
        const val ARSC_HEADER_SIZE: Int = 12
        const val AXML_HEADER_SIZE: Int = 8
    }
}
