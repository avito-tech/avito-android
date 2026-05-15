package com.avito.android.string_transform.internal.task

import com.avito.android.string_transform.createApkFixture
import com.avito.android.string_transform.createArscBytes
import com.avito.android.string_transform.createBinaryAxmlBytes
import com.avito.android.string_transform.createDexBytes
import com.avito.android.string_transform.internal.rules.NormalizedRule
import com.avito.android.string_transform.internal.task.apk.StringPoolCodec
import com.avito.android.string_transform.primaryClassType
import com.avito.android.string_transform.readDexFile
import com.avito.android.string_transform.readEntryBytes
import com.avito.android.string_transform.readEntryText
import com.avito.android.string_transform.task.TransformVariantApkTask
import com.google.common.truth.Truth.assertThat
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.ZipFile

internal class TransformVariantApkTaskGradleTest {

    private lateinit var project: Project

    @BeforeEach
    fun setUp(@TempDir dir: File) {
        project = ProjectBuilder.builder()
            .withProjectDir(dir)
            .build()
    }

    @Test
    fun `apk task - writes successful report and publishes transformed apk - when input apk is valid`() {
        val task = taskUnderTest(
            rules = listOf(NormalizedRule(from = "samplevalue", to = "changedvalue")),
        )
        val inputApkFile = apkInputDirectory().resolve("input.apk")
        inputApkFile.parentFile.mkdirs()
        inputApkFile.writeBytes(
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
                    "assets/samplevalue.txt" to "samplevalue inside an asset\n".toByteArray(),
                    "META-INF/CERT.RSA" to byteArrayOf(0x01, 0x02, 0x03),
                    "META-INF/services/com.example.samplevalue.Service" to
                        "com.example.samplevalue.ServiceImpl\n".toByteArray(),
                ),
            ),
        )

        task.transform()

        assertThat(outputApkFile().exists()).isTrue()

        ZipFile(outputApkFile()).use { zip ->
            val manifestStrings = StringPoolCodec
                .parse(zip.readEntryBytes("AndroidManifest.xml"), chunkOffset = AXML_HEADER_SIZE)
                .pool
                .strings
            assertThat(manifestStrings).contains("changedvalue")
            assertThat(manifestStrings).doesNotContain("samplevalue")

            val arscBytes = zip.readEntryBytes("resources.arsc")
            val arscTopPool = StringPoolCodec.parse(arscBytes, chunkOffset = ARSC_HEADER_SIZE)
            assertThat(arscTopPool.pool.strings).containsExactly("changedvalue", "untouched").inOrder()

            val packageOffset = ARSC_HEADER_SIZE + arscTopPool.chunkSize
            val packageBuffer = ByteBuffer.wrap(arscBytes).order(ByteOrder.LITTLE_ENDIAN)
            val keyStringsOffset = packageBuffer.getInt(packageOffset + 276)
            val packageKeyStrings = StringPoolCodec
                .parse(arscBytes, packageOffset + keyStringsOffset)
                .pool
                .strings
            assertThat(packageKeyStrings)
                .containsExactly("changedvalue_title", "untouched_name")
                .inOrder()

            val dexFile = readDexFile(
                bytes = zip.readEntryBytes("classes.dex"),
                tempDirectory = project.layout.buildDirectory.dir("apk-dex-inspection").get().asFile,
                name = "classes.dex",
            )
            assertThat(dexFile.primaryClassType()).isEqualTo("Lcom/example/changedvalue/Holder;")

            assertThat(zip.getEntry("assets/samplevalue.txt")).isNull()
            assertThat(zip.getEntry("assets/changedvalue.txt")).isNotNull()
            assertThat(zip.readEntryText("assets/changedvalue.txt"))
                .isEqualTo("changedvalue inside an asset\n")

            assertThat(zip.getEntry("META-INF/CERT.RSA")).isNull()
            assertThat(zip.getEntry("META-INF/services/com.example.samplevalue.Service")).isNull()
            assertThat(zip.readEntryText("META-INF/services/com.example.changedvalue.Service"))
                .isEqualTo("com.example.changedvalue.ServiceImpl\n")
        }

        assertReportJsonMatches(
            reportFile().readText(),
            literalJsonPattern("""
            {
                "pipeline": "alpha",
                "variant": "release",
                "artifact": {
                    "moduleIdentity": ":app",
                    "variantIdentity": "release"
                },
                "rules": {
                    "totalRules": 1,
                    "declarationCounts": {
                        "exact": 1,
                        "caseExpanded": 0
                    }
                },
                "phases": [
                    {
                        "name": "variant-apk-outputs-observation",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "input-apk-resolution",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "variant-apk-artifact-observation",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "input-apk-validation",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "apk-unpack",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "arsc-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "binary-axml-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "dex-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "kotlin-module-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "residual-text-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "rename",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "metadata-cleanup",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "apk-repack",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "output-publication",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    }
                ],
                "diagnostics": [\E\s*\Q]
            }
            """)
        )
    }

    @Test
    fun `apk task - writes failure report and rethrows - when input apk directory contains multiple candidates`() {
        val task = taskUnderTest()
        val apkInputDir = apkInputDirectory()
        apkInputDir.mkdirs()
        File(apkInputDir, "app-arm64.apk").writeText("stub apk")
        File(apkInputDir, "app-universal.apk").writeText("stub apk")

        val error = assertThrows(IllegalArgumentException::class.java) {
            task.transform()
        }

        assertThat(error.message).contains("exactly one publishable APK candidate")
        assertReportJsonMatches(
            reportFile().readText(),
            literalJsonPattern("""
            {
                "pipeline": "alpha",
                "variant": "release",
                "artifact": {
                    "moduleIdentity": ":app",
                    "variantIdentity": "release"
                },
                "rules": {
                    "totalRules": 1,
                    "declarationCounts": {
                        "exact": 1,
                        "caseExpanded": 0
                    }
                },
                "phases": [
                    {
                        "name": "variant-apk-outputs-observation",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "input-apk-resolution",
                        "status": "FAILURE",
                        "durationMillis": \E\d+\Q
                    }
                ],
                "diagnostics": [
                    {
                        "severity": "HARD_FAILURE",
                        "message": "Observed VariantApkOutputs do not contain exactly one publishable APK candidate: [${apkInputDir.path}/app-arm64.apk, ${apkInputDir.path}/app-universal.apk]",
                        "affectedPhase": "input-apk-resolution",
                        "affectedPath": null
                    }
                ]
            }
            """)
        )
    }

    @Test
    fun `apk task - writes failure report and rethrows - when apk outputs path is not a directory`() {
        val task = taskUnderTest()
        val apkInputFile = apkInputDirectory()
        apkInputFile.parentFile.mkdirs()
        apkInputFile.writeText("not a directory")

        val error = assertThrows(IllegalStateException::class.java) {
            task.transform()
        }

        assertThat(error.message).contains("VariantApkOutputs path is not a directory")
        assertReportJsonMatches(
            reportFile().readText(),
            literalJsonPattern("""
            {
                "pipeline": "alpha",
                "variant": "release",
                "artifact": {
                    "moduleIdentity": ":app",
                    "variantIdentity": "release"
                },
                "rules": {
                    "totalRules": 1,
                    "declarationCounts": {
                        "exact": 1,
                        "caseExpanded": 0
                    }
                },
                "phases": [
                    {
                        "name": "variant-apk-outputs-observation",
                        "status": "FAILURE",
                        "durationMillis": \E\d+\Q
                    }
                ],
                "diagnostics": [
                    {
                        "severity": "HARD_FAILURE",
                        "message": "VariantApkOutputs path is not a directory: ${apkInputFile.path}",
                        "affectedPhase": "variant-apk-outputs-observation",
                        "affectedPath": null
                    }
                ]
            }
            """)
        )
    }

    @Test
    fun `apk task - writes failure report and rethrows - when input apk cannot be resolved`() {
        val task = taskUnderTest()
        apkInputDirectory().mkdirs()

        val error = assertThrows(IllegalArgumentException::class.java) {
            task.transform()
        }

        assertThat(error.message).contains("exactly one publishable APK candidate")
        assertReportJsonMatches(
            reportFile().readText(),
            literalJsonPattern("""
            {
                "pipeline": "alpha",
                "variant": "release",
                "artifact": {
                    "moduleIdentity": ":app",
                    "variantIdentity": "release"
                },
                "rules": {
                    "totalRules": 1,
                    "declarationCounts": {
                        "exact": 1,
                        "caseExpanded": 0
                    }
                },
                "phases": [
                    {
                        "name": "variant-apk-outputs-observation",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "input-apk-resolution",
                        "status": "FAILURE",
                        "durationMillis": \E\d+\Q
                    }
                ],
                "diagnostics": [
                    {
                        "severity": "HARD_FAILURE",
                        "message": "Observed VariantApkOutputs do not contain exactly one publishable APK candidate: []",
                        "affectedPhase": "input-apk-resolution",
                        "affectedPath": null
                    }
                ]
            }
            """)
        )
    }

    private fun taskUnderTest(
        rules: List<NormalizedRule> = emptyList(),
    ): TransformVariantApkTask {
        val task = project.tasks.register(
            "transformStringsAlphaRelease",
            TransformVariantApkTask::class.java,
        ).get()

        with(task) {
            modulePath.set(":app")
            pipelineName.set("alpha")
            variantName.set("release")
            totalRuleCount.set(1)
            exactRuleCount.set(1)
            caseExpandedRuleCount.set(0)
            configurationWarnings.set(emptyList())
            this.rules.set(rules)
            apkDirectory.set(project.layout.buildDirectory.dir("apk-input/release"))
            localStateDirectory.set(project.layout.buildDirectory.dir("tmp/transformStrings/alpha/release/local-state"))
            outputApkFile.set(
                project.layout.buildDirectory.file(
                    "outputs/transformStrings/alpha/release/apk/transformed-unsigned.apk"
                )
            )
            reportFile.set(
                project.layout.buildDirectory.file(
                    "outputs/transformStrings/alpha/release/report/transform-report.json"
                )
            )
        }

        return task
    }

    private fun apkInputDirectory(): File {
        return project.layout.buildDirectory.dir("apk-input/release").get().asFile
    }

    private fun reportFile(): File {
        return project.layout.buildDirectory
            .file("outputs/transformStrings/alpha/release/report/transform-report.json")
            .get()
            .asFile
    }

    private fun outputApkFile(): File {
        return project.layout.buildDirectory
            .file("outputs/transformStrings/alpha/release/apk/transformed-unsigned.apk")
            .get()
            .asFile
    }

    private fun assertReportJsonMatches(report: String, expectedRegex: String) {
        assertThat(report.trim()).containsMatch(expectedRegex.trimIndent())
    }

    private fun literalJsonPattern(jsonBody: String): String {
        return "(?s)^\\Q${jsonBody.trimIndent()}\\E$"
    }

    private companion object {
        const val ARSC_HEADER_SIZE: Int = 12
        const val AXML_HEADER_SIZE: Int = 8
    }
}
