package com.avito.android.string_transform.internal.task

import com.android.aapt.Resources
import com.avito.android.string_transform.asTextFormat
import com.avito.android.string_transform.createAabFixture
import com.avito.android.string_transform.internal.rules.NormalizedRule
import com.avito.android.string_transform.primaryClassSourceFile
import com.avito.android.string_transform.primaryClassType
import com.avito.android.string_transform.primaryFieldInitialStringValue
import com.avito.android.string_transform.primaryFieldName
import com.avito.android.string_transform.primaryMethodConstString
import com.avito.android.string_transform.primaryMethodName
import com.avito.android.string_transform.primaryMethodParameterName
import com.avito.android.string_transform.readDexFile
import com.avito.android.string_transform.readEntryBytes
import com.avito.android.string_transform.task.TransformVariantAabTask
import com.google.common.truth.Truth.assertThat
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.zip.ZipFile

internal class TransformVariantAabTaskGradleTest {

    private lateinit var project: Project

    @BeforeEach
    fun setUp(@TempDir dir: File) {
        project = ProjectBuilder.builder()
            .withProjectDir(dir)
            .build()
    }

    @Test
    fun `aab task - writes successful report and publishes transformed bundle - when input aab is valid`() {
        val task = taskUnderTest()
        createAabFixture(inputAabFile())
        val originalBundleConfigBytes = ZipFile(inputAabFile()).use { zip ->
            zip.readEntryBytes("BundleConfig.pb")
        }
        val originalNativePbBytes = ZipFile(inputAabFile()).use { zip ->
            zip.readEntryBytes("base/native.pb")
        }

        task.transform()

        assertThat(outputAabFile().exists()).isTrue()
        ZipFile(outputAabFile()).use { zip ->
            assertThat(zip.readEntryBytes("BundleConfig.pb")).isEqualTo(originalBundleConfigBytes)
            assertThat(zip.readEntryBytes("base/native.pb")).isEqualTo(originalNativePbBytes)

            assertThat(zip.getEntry("base/assets/changedvalue.txt")).isNotNull()
            assertThat(zip.getEntry("base/assets/samplevalue.txt")).isNull()
            assertThat(zip.readEntry("base/assets/changedvalue.txt").toString(Charsets.UTF_8))
                .isEqualTo("changedvalue")

            assertThat(zip.getEntry("base/res/raw/changedvalue.xml")).isNotNull()
            assertThat(zip.getEntry("base/res/raw/samplevalue.xml")).isNull()
            assertThat(zip.readEntry("base/res/raw/changedvalue.xml").toString(Charsets.UTF_8))
                .isEqualTo("changedvalue")

            assertThat(zip.getEntry("base/res/xml/changedvalue_config.xml")).isNotNull()
            assertThat(zip.getEntry("base/res/xml/samplevalue_config.xml")).isNull()

            assertThat(zip.getEntry("META-INF/BNDLTOOL.SF")).isNull()
            assertThat(zip.getEntry("META-INF/services/demo.Service")).isNotNull()
            assertThat(zip.readEntry("META-INF/services/demo.Service").toString(Charsets.UTF_8))
                .isEqualTo("implementation")

            val resourcesPb = Resources.ResourceTable.parseFrom(
                zip.readEntryBytes("base/resources.pb")
            )
            assertThat(resourcesPb.asTextFormat()).contains("changedvalue")
            assertThat(resourcesPb.asTextFormat()).doesNotContain("samplevalue")

            val manifestXml = Resources.XmlNode.parseFrom(
                zip.readEntryBytes("base/manifest/AndroidManifest.xml")
            )
            assertThat(manifestXml.asTextFormat()).contains("changedvalue")
            assertThat(manifestXml.asTextFormat()).doesNotContain("samplevalue")

            val compiledXml = Resources.XmlNode.parseFrom(
                zip.readEntryBytes("base/res/xml/changedvalue_config.xml")
            )
            assertThat(compiledXml.asTextFormat()).contains("changedvalue")
            assertThat(compiledXml.asTextFormat()).doesNotContain("samplevalue")

            val dexFile = readDexFile(
                bytes = zip.readEntryBytes("base/dex/classes.dex"),
                tempDirectory = project.layout.buildDirectory.dir("aab-dex-inspection").get().asFile,
                name = "classes.dex",
            )
            assertThat(dexFile.primaryClassType()).isEqualTo("Lcom/example/changedvalue/Holder;")
            assertThat(dexFile.primaryClassSourceFile()).isEqualTo("changedvalue.kt")
            assertThat(dexFile.primaryFieldName()).isEqualTo("changedvalueField")
            assertThat(dexFile.primaryFieldInitialStringValue()).isEqualTo("changedvalue")
            assertThat(dexFile.primaryMethodName()).isEqualTo("changedvalueMethod")
            assertThat(dexFile.primaryMethodParameterName()).isEqualTo("changedvalueParam")
            assertThat(dexFile.primaryMethodConstString()).isEqualTo("changedvalue")
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
                        "name": "variant-aab-artifact-observation",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "input-aab-validation",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "bundle-unpack",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "resources-pb-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "protobuf-xml-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "dex-transform",
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
                        "name": "bundle-repack",
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
    fun `aab task - writes failure report and rethrows - when input bundle path does not exist`() {
        val task = taskUnderTest()

        val error = assertThrows(IllegalStateException::class.java) {
            task.transform()
        }

        assertThat(error.message).contains("Variant bundle artifact file does not exist")
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
                        "name": "variant-aab-artifact-observation",
                        "status": "FAILURE",
                        "durationMillis": \E\d+\Q
                    }
                ],
                "diagnostics": [
                    {
                        "severity": "HARD_FAILURE",
                        "message": "Variant bundle artifact file does not exist: ${inputAabFile().path}",
                        "affectedPhase": "variant-aab-artifact-observation",
                        "affectedPath": null
                    }
                ]
            }
            """)
        )
    }

    @Test
    fun `aab task - writes failure report and rethrows - when input file is not a publishable aab`() {
        val task = taskUnderTest()
        val inputBundle = inputAabFile().parentFile.resolve("input.apk").apply {
            parentFile.mkdirs()
            writeText("not aab")
        }
        task.inputAabFile.set(inputBundle)

        val error = assertThrows(IllegalArgumentException::class.java) {
            task.transform()
        }

        assertThat(error.message).contains("is not a publishable AAB candidate")
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
                        "name": "variant-aab-artifact-observation",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "input-aab-validation",
                        "status": "FAILURE",
                        "durationMillis": \E\d+\Q
                    }
                ],
                "diagnostics": [
                    {
                        "severity": "HARD_FAILURE",
                        "message": "Observed bundle artifact is not a publishable AAB candidate: ${inputBundle.path}",
                        "affectedPhase": "input-aab-validation",
                        "affectedPath": null
                    }
                ]
            }
            """)
        )
    }

    private fun taskUnderTest(): TransformVariantAabTask {
        val task = project.tasks.register(
            "transformStringsAlphaReleaseBundle",
            TransformVariantAabTask::class.java,
        ).get()

        with(task) {
            modulePath.set(":app")
            pipelineName.set("alpha")
            variantName.set("release")
            totalRuleCount.set(1)
            exactRuleCount.set(1)
            caseExpandedRuleCount.set(0)
            configurationWarnings.set(emptyList())
            rules.set(listOf(NormalizedRule(from = "samplevalue", to = "changedvalue")))
            inputAabFile.set(inputAabFile())
            localStateDirectory.set(
                project.layout.buildDirectory.dir("tmp/transformStrings/alpha/release-bundle/local-state")
            )
            outputAabFile.set(
                project.layout.buildDirectory.file(
                    "outputs/transformStrings/alpha/release/aab/transformed-unsigned.aab"
                )
            )
            reportFile.set(
                project.layout.buildDirectory.file(
                    "outputs/transformStrings/alpha/release/aab/report/transform-report.json"
                )
            )
        }

        return task
    }

    private fun inputAabFile(): File {
        return project.layout.buildDirectory.file("aab-input/release/input.aab").get().asFile
    }

    private fun reportFile(): File {
        return project.layout.buildDirectory
            .file("outputs/transformStrings/alpha/release/aab/report/transform-report.json")
            .get()
            .asFile
    }

    private fun outputAabFile(): File {
        return project.layout.buildDirectory
            .file("outputs/transformStrings/alpha/release/aab/transformed-unsigned.aab")
            .get()
            .asFile
    }

    private fun assertReportJsonMatches(report: String, expectedRegex: String) {
        assertThat(report.trim()).containsMatch(expectedRegex.trimIndent())
    }

    private fun literalJsonPattern(jsonBody: String): String {
        return "(?s)^\\Q${jsonBody.trimIndent()}\\E$"
    }

    private fun ZipFile.readEntry(path: String): ByteArray {
        val entry = checkNotNull(getEntry(path)) {
            "Zip entry not found: $path"
        }
        return getInputStream(entry).use { it.readBytes() }
    }
}
