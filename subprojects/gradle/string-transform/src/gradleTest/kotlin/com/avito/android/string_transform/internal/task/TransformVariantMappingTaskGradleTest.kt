package com.avito.android.string_transform.internal.task

import com.avito.android.string_transform.internal.rules.NormalizedRule
import com.avito.android.string_transform.task.TransformVariantMappingTask
import com.google.common.truth.Truth.assertThat
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class TransformVariantMappingTaskGradleTest {

    private lateinit var project: Project

    @BeforeEach
    fun setUp(@TempDir dir: File) {
        project = ProjectBuilder.builder()
            .withProjectDir(dir)
            .build()
    }

    @Test
    fun `mapping task - writes successful report and publishes transformed mapping - when input mapping is valid`() {
        val task = taskUnderTest()
        inputMappingFile().apply {
            parentFile.mkdirs()
            writeText(VALID_MAPPING)
        }

        task.transform()

        assertThat(outputMappingFile().exists()).isTrue()
        assertThat(outputMappingFile().readText()).contains("changedvalue")
        assertThat(outputMappingFile().readText()).doesNotContain("samplevalue")
        assertReportJsonMatches(
            reportFile().readText(),
            literalJsonPattern(
                """
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
                        "name": "variant-mapping-artifact-observation",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "input-mapping-validation",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "mapping-text-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "mapping-structural-sanity-validation",
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
            """
            )
        )
    }

    @Test
    fun `mapping task - writes failure report and rethrows - when input mapping path does not exist`() {
        val task = taskUnderTest()

        val error = assertThrows(IllegalStateException::class.java) {
            task.transform()
        }

        assertThat(error.message).contains("Variant mapping artifact file does not exist")
        assertReportJsonMatches(
            reportFile().readText(),
            literalJsonPattern(
                """
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
                        "name": "variant-mapping-artifact-observation",
                        "status": "FAILURE",
                        "durationMillis": \E\d+\Q
                    }
                ],
                "diagnostics": [
                    {
                        "severity": "HARD_FAILURE",
                        "message": "Variant mapping artifact file does not exist: ${inputMappingFile().path}",
                        "affectedPhase": "variant-mapping-artifact-observation",
                        "affectedPath": null
                    }
                ]
            }
            """
            )
        )
    }

    @Test
    fun `mapping task - writes failure report and rethrows - when transformed mapping is structurally invalid`() {
        val task = taskUnderTest()
        inputMappingFile().apply {
            parentFile.mkdirs()
            writeText("not a mapping")
        }

        val error = assertThrows(IllegalStateException::class.java) {
            task.transform()
        }

        assertThat(error.message).contains("Transformed mapping failed structural sanity validation")
        assertReportJsonMatches(
            reportFile().readText(),
            literalJsonPattern(
                """
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
                        "name": "variant-mapping-artifact-observation",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "input-mapping-validation",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "mapping-text-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "mapping-structural-sanity-validation",
                        "status": "FAILURE",
                        "durationMillis": \E\d+\Q
                    }
                ],
                "diagnostics": [
                    {
                        "severity": "HARD_FAILURE",
                        "message": "Transformed mapping failed structural sanity validation: ${
                    project.layout.buildDirectory.dir(
                        "tmp/transformStrings/alpha/release/mapping-local-state"
                    ).get().asFile.resolve("transformed-mapping.txt").path
                }",
                        "affectedPhase": "mapping-structural-sanity-validation",
                        "affectedPath": null
                    }
                ]
            }
            """
            )
        )
    }

    private fun taskUnderTest(): TransformVariantMappingTask {
        val task = project.tasks.register(
            "transformStringsAlphaReleaseMapping",
            TransformVariantMappingTask::class.java,
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
            inputMappingFile.set(inputMappingFile())
            localStateDirectory.set(
                project.layout.buildDirectory.dir("tmp/transformStrings/alpha/release/mapping-local-state")
            )
            outputMappingFile.set(
                project.layout.buildDirectory.file(
                    "outputs/transformStrings/alpha/release/mapping/transformed-mapping.txt"
                )
            )
            reportFile.set(
                project.layout.buildDirectory.file(
                    "outputs/transformStrings/alpha/release/mapping/report/transform-report.json"
                )
            )
        }

        return task
    }

    private fun inputMappingFile(): File {
        return project.layout.buildDirectory.file("mapping-input/release/mapping.txt").get().asFile
    }

    private fun reportFile(): File {
        return project.layout.buildDirectory
            .file("outputs/transformStrings/alpha/release/mapping/report/transform-report.json")
            .get()
            .asFile
    }

    private fun outputMappingFile(): File {
        return project.layout.buildDirectory
            .file("outputs/transformStrings/alpha/release/mapping/transformed-mapping.txt")
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
        private const val VALID_MAPPING = """
# compiler: R8
com.example.samplevalue.Holder -> a:
    java.lang.String samplevalueField -> a
    void samplevalueMethod(java.lang.String) -> a
"""
    }
}
