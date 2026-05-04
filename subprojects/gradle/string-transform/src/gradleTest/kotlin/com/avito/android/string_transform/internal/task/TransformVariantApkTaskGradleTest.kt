package com.avito.android.string_transform.internal.task

import com.avito.android.string_transform.task.TransformVariantApkTask
import com.google.common.truth.Truth.assertThat
import org.gradle.api.Project
import org.gradle.jvm.toolchain.JavaInstallationMetadata
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class TransformVariantApkTaskGradleTest {

    private lateinit var project: Project

    @BeforeEach
    fun setUp(@TempDir dir: File) {
        project = ProjectBuilder.builder()
            .withProjectDir(dir)
            .build()
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

    @Test
    fun `apk task - writes failure report and rethrows - when apktool jar is unavailable`() {
        val task = taskUnderTest().also {
            it.apktoolClasspath.from(project.layout.projectDirectory.file("missing-apktool.jar"))
        }
        val apkInputDir = apkInputDirectory()
        apkInputDir.mkdirs()
        File(apkInputDir, "app-release.apk").writeText("stub apk")

        val error = assertThrows(IllegalStateException::class.java) {
            task.transform()
        }

        assertThat(error.message).contains("apktool jar is not available")
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
                        "name": "apktool-decode",
                        "status": "FAILURE",
                        "durationMillis": \E\d+\Q
                    }
                ],
                "diagnostics": [
                    {
                        "severity": "HARD_FAILURE",
                        "message": "apktool jar is not available: ${project.projectDir.path}/missing-apktool.jar",
                        "affectedPhase": "apktool-decode",
                        "affectedPath": null
                    }
                ]
            }
            """)
        )
    }

    private fun taskUnderTest(): TransformVariantApkTask {
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
            rules.set(emptyList())
            javaLauncher.set(fakeJavaLauncher())
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

    private fun fakeJavaLauncher(): JavaLauncher {
        return object : JavaLauncher {
            override fun getExecutablePath() = project.layout.projectDirectory.file("fake-java")

            override fun getMetadata(): JavaInstallationMetadata {
                throw UnsupportedOperationException("metadata is not used in this test")
            }
        }
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

    private fun assertReportJsonMatches(report: String, expectedRegex: String) {
        assertThat(report.trim()).containsMatch(expectedRegex.trimIndent())
    }

    private fun literalJsonPattern(jsonBody: String): String {
        return "(?s)^\\Q${jsonBody.trimIndent()}\\E$"
    }
}
