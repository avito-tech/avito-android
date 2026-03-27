package com.avito.android.string_transform.internal.task

import com.google.common.truth.Truth.assertThat
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class TransformVariantAabTaskGradleTest {

    private lateinit var project: Project

    @BeforeEach
    fun setUp(@TempDir dir: File) {
        project = ProjectBuilder.builder()
            .withProjectDir(dir)
            .build()
    }

    @Test
    fun `aab task - writes successful report - when input aab is valid`() {
        val task = taskUnderTest()
        inputAabFile().apply {
            parentFile.mkdirs()
            writeText("bundle")
        }

        task.transform()

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
            inputAabFile.set(inputAabFile())
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

    private fun assertReportJsonMatches(report: String, expectedRegex: String) {
        assertThat(report.trim()).containsMatch(expectedRegex.trimIndent())
    }

    private fun literalJsonPattern(jsonBody: String): String {
        return "(?s)^\\Q${jsonBody.trimIndent()}\\E$"
    }
}
