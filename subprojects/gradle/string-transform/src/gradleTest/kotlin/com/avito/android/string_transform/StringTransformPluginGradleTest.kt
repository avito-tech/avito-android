package com.avito.android.string_transform

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class StringTransformPluginGradleTest {

    private lateinit var projectDir: File

    @BeforeEach
    fun setUp(@TempDir dir: File) {
        projectDir = dir
    }

    @Test
    fun `plugin root task - succeeds without outputs - when no pipelines are declared`() {
        givenProject(
            """
            transformStrings {
            }
            """.trimIndent()
        )

        gradlew(
            projectDir,
            ":app:transformStrings",
            useTestFixturesClasspath = true,
        ).assertThat().buildSuccessful()

        val outputsDir = File(
            projectDir,
            "app/build/outputs/transformStrings"
        )
        assertThat(outputsDir.exists()).isFalse()
    }

    @Test
    fun `plugin tasks - register variant task and report - when exact release variant matches`() {
        givenProject(
            """
            transformStrings {
                create("alpha") {
                    variant("release")
                    rules {
                        exact("samplevalue", "changedvalue")
                    }
                }
            }
            """.trimIndent()
        )

        gradlew(
            projectDir,
            ":app:transformStrings",
            useTestFixturesClasspath = true,
        ).assertThat().buildSuccessful()

        val reportFile = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/release/report/transform-report.json"
        )

        assertThat(reportFile.exists()).isTrue()
        assertSuccessfulReportText(
            report = reportFile.readText(),
            pipeline = "alpha",
            variant = "release",
            totalRules = 1,
            exactDeclarations = 1,
            caseExpandedDeclarations = 0,
        )
        assertThat(
            File(
                projectDir,
                "app/build/outputs/transformStrings/alpha/release/apk/transformed-unsigned.apk"
            ).exists()
        ).isFalse()
    }

    @Test
    fun `plugin tasks - register variant task and report - when exact flavored variant matches`() {
        givenProject(
            """
            android {
                flavorDimensions += "tier"
                productFlavors {
                    create("free") {
                        dimension = "tier"
                    }
                    create("paid") {
                        dimension = "tier"
                    }
                }
            }

            transformStrings {
                create("alpha") {
                    variant("paidRelease")
                    rules {
                        exact("samplevalue", "changedvalue")
                    }
                }
            }
            """.trimIndent()
        )

        gradlew(
            projectDir,
            ":app:transformStrings",
            useTestFixturesClasspath = true,
        ).assertThat().buildSuccessful()

        val reportFile = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/paidRelease/report/transform-report.json"
        )
        assertThat(reportFile.exists()).isTrue()
        assertSuccessfulReportText(
            report = reportFile.readText(),
            pipeline = "alpha",
            variant = "paidRelease",
            totalRules = 1,
            exactDeclarations = 1,
            caseExpandedDeclarations = 0,
        )
    }

    @Test
    fun `plugin report - writes rule summary - when exact and case-expanded rules are declared`() {
        givenProject(
            """
            transformStrings {
                create("alpha") {
                    variant("release")
                    rules {
                        exact("samplevalue", "changedvalue")
                        caseExpanded(
                            "token",
                            "value",
                            com.avito.android.string_transform.GeneratedForm.LOWER,
                            com.avito.android.string_transform.GeneratedForm.UPPER,
                            com.avito.android.string_transform.GeneratedForm.UPPER_FIRST,
                        )
                    }
                }
            }
            """.trimIndent()
        )

        gradlew(
            projectDir,
            ":app:transformStrings",
            useTestFixturesClasspath = true,
        ).assertThat().buildSuccessful()

        val reportFile = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/release/report/transform-report.json"
        )

        assertThat(reportFile.exists()).isTrue()
        assertSuccessfulReportText(
            report = reportFile.readText(),
            pipeline = "alpha",
            variant = "release",
            totalRules = 4,
            exactDeclarations = 1,
            caseExpandedDeclarations = 1,
        )
    }

    @Test
    fun `plugin report - uses final pipeline configuration - when rules are added after create call`() {
        givenProject(
            """
            transformStrings {
                val pipeline = create("alpha") {
                    variant("release")
                    rules {
                        exact("samplevalue", "changedvalue")
                    }
                }
                pipeline.rules {
                    caseExpanded(
                        "token",
                        "value",
                        com.avito.android.string_transform.GeneratedForm.UPPER_FIRST,
                        com.avito.android.string_transform.GeneratedForm.LOWER,
                    )
                }
            }
            """.trimIndent()
        )

        gradlew(
            projectDir,
            ":app:transformStrings",
            useTestFixturesClasspath = true,
        ).assertThat().buildSuccessful()

        val reportFile = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/release/report/transform-report.json"
        )

        assertThat(reportFile.exists()).isTrue()
        assertSuccessfulReportText(
            report = reportFile.readText(),
            pipeline = "alpha",
            variant = "release",
            totalRules = 3,
            exactDeclarations = 1,
            caseExpandedDeclarations = 1,
        )
    }

    @Test
    fun `plugin tasks - use final pipeline configuration - when variant is set after create call`() {
        givenProject(
            """
            transformStrings {
                val pipeline = create("alpha") {
                    rules {
                        exact("samplevalue", "changedvalue")
                    }
                }
                pipeline.variant("release")
            }
            """.trimIndent()
        )

        gradlew(
            projectDir,
            ":app:transformStrings",
            useTestFixturesClasspath = true,
        ).assertThat().buildSuccessful()

        val reportFile = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/release/report/transform-report.json"
        )

        assertThat(reportFile.exists()).isTrue()
        assertSuccessfulReportText(
            report = reportFile.readText(),
            pipeline = "alpha",
            variant = "release",
            totalRules = 1,
            exactDeclarations = 1,
            caseExpandedDeclarations = 0,
        )
    }

    @Test
    fun `plugin root task - executes all matched variant tasks - when multiple pipelines target the same variant`() {
        givenProject(
            """
            transformStrings {
                create("alpha") {
                    variant("release")
                    rules {
                        exact("samplevalue", "changedvalue")
                    }
                }
                create("beta") {
                    variant("release")
                    rules {
                        caseExpanded(
                            "token",
                            "value",
                            com.avito.android.string_transform.GeneratedForm.LOWER,
                        )
                    }
                }
            }
            """.trimIndent()
        )

        gradlew(
            projectDir,
            ":app:transformStrings",
            useTestFixturesClasspath = true,
        ).assertThat().buildSuccessful()

        val alphaReport = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/release/report/transform-report.json"
        )
        val betaReport = File(
            projectDir,
            "app/build/outputs/transformStrings/beta/release/report/transform-report.json"
        )

        assertThat(alphaReport.exists()).isTrue()
        assertThat(betaReport.exists()).isTrue()
        assertSuccessfulReportText(
            report = alphaReport.readText(),
            pipeline = "alpha",
            variant = "release",
            totalRules = 1,
            exactDeclarations = 1,
            caseExpandedDeclarations = 0,
        )
        assertSuccessfulReportText(
            report = betaReport.readText(),
            pipeline = "beta",
            variant = "release",
            totalRules = 1,
            exactDeclarations = 0,
            caseExpandedDeclarations = 1,
        )
    }

    @Test
    fun `plugin configuration cache - reuses entry - when configuration is unchanged`() {
        givenProject(
            """
            transformStrings {
                create("alpha") {
                    variant("release")
                    rules {
                        exact("samplevalue", "changedvalue")
                    }
                }
            }
            """.trimIndent()
        )

        gradlew(
            projectDir,
            ":app:transformStrings",
            configurationCache = true,
            useTestFixturesClasspath = true,
        ).assertThat().buildSuccessful()

        gradlew(
            projectDir,
            ":app:transformStrings",
            configurationCache = true,
            useTestFixturesClasspath = true,
        ).assertThat().buildSuccessful().configurationCachedReused()
    }

    @Test
    fun `plugin registration - fails build - when pipeline variant is not configured`() {
        givenProject(
            """
            transformStrings {
                create("invalid") {
                    rules {
                        exact("samplevalue", "changedvalue")
                    }
                }
            }
            """.trimIndent()
        )

        val output = gradlew(
            projectDir,
            ":app:help",
            expectFailure = true,
            useTestFixturesClasspath = true,
        ).output

        assertThat(output).contains("String-transform pipeline 'invalid' does not declare variant")
    }

    @Test
    fun `plugin registration - fails build - when rule declaration is malformed`() {
        givenProject(
            """
            transformStrings {
                create("invalid") {
                    variant("release")
                    rules {
                        exact("", "target")
                    }
                }
            }
            """.trimIndent()
        )

        val output = gradlew(
            projectDir,
            ":app:help",
            expectFailure = true,
            useTestFixturesClasspath = true,
        ).output

        assertThat(output).contains("transformStrings rule 'from' value must not be empty")
    }

    @Test
    fun `plugin registration - fails build - when case-expanded rule has no generated forms`() {
        givenProject(
            """
            transformStrings {
                create("invalid") {
                    variant("release")
                    rules {
                        caseExpanded("source", "target")
                    }
                }
            }
            """.trimIndent()
        )

        val output = gradlew(
            projectDir,
            ":app:help",
            expectFailure = true,
            useTestFixturesClasspath = true,
        ).output

        assertThat(output).contains("transformStrings caseExpanded rule must declare at least one generated form")
    }

    // This permissive behavior is accepted only for v1
    // TODO (rm): introduce explicit validation and a clear error later.
    @Test
    fun `plugin registration - allows pipeline name - when pipeline name contains spaces`() {
        givenProject(
            """
            transformStrings {
                create("bad name") {
                    variant("release")
                    rules {
                        exact("samplevalue", "changedvalue")
                    }
                }
            }
            """.trimIndent()
        )

        gradlew(
            projectDir,
            ":app:transformStrings",
            useTestFixturesClasspath = true,
        ).assertThat().buildSuccessful()
    }

    // This permissive behavior is accepted only for v1
    // TODO (rm): introduce explicit validation and a clear error later.
    @Test
    fun `plugin tasks - do not register variant task - when exact variant name is unknown`() {
        givenProject(
            """
            transformStrings {
                create("missing") {
                    variant("missingRelease")
                    rules {
                        exact("samplevalue", "changedvalue")
                    }
                }
            }
            """.trimIndent()
        )

        val output = gradlew(
            projectDir,
            ":app:tasks",
            "--all",
            useTestFixturesClasspath = true,
        ).output

        assertThat(output).doesNotContain("transformStringsMissingMissingRelease")
    }

    @Test
    fun `plugin tasks - do not register unrelated variant task - when only release variant is selected`() {
        givenProject(
            """
            transformStrings {
                create("alpha") {
                    variant("release")
                    rules {
                        exact("samplevalue", "changedvalue")
                    }
                }
            }
            """.trimIndent()
        )

        val output = gradlew(
            projectDir,
            ":app:tasks",
            "--all",
            useTestFixturesClasspath = true,
        ).output

        assertThat(output).contains("transformStringsAlphaRelease")
        assertThat(output).doesNotContain("transformStringsAlphaDebug")
    }

    @Test
    fun `plugin root task - succeeds without report - when exact variant name is unknown`() {
        givenProject(
            """
            transformStrings {
                create("missing") {
                    variant("missingRelease")
                    rules {
                        exact("samplevalue", "changedvalue")
                    }
                }
            }
            """.trimIndent()
        )

        gradlew(
            projectDir,
            ":app:transformStrings",
            useTestFixturesClasspath = true,
        ).assertThat().buildSuccessful()

        val reportFile = File(
            projectDir,
            "app/build/outputs/transformStrings/missing/missingRelease/report/transform-report.json"
        )
        assertThat(reportFile.exists()).isFalse()
    }

    @Test
    fun `plugin warnings - print warning and record diagnostic - when matched pipeline contains overlapping rules`() {
        givenProject(
            """
            transformStrings {
                create("alpha") {
                    variant("release")
                    rules {
                        exact("token", "value")
                        exact("tok", "prefix")
                    }
                }
            }
            """.trimIndent()
        )

        val result = gradlew(
            projectDir,
            ":app:transformStrings",
            useTestFixturesClasspath = true,
        )

        assertThat(result.output).contains("overlap and remain order-sensitive")
        val reportFile = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/release/report/transform-report.json"
        )
        assertReportJsonMatches(
            report = reportFile.readText(),
            expectedRegex = literalJsonPattern("""
            {
                "pipeline": "alpha",
                "variant": "release",
                "artifact": {
                    "moduleIdentity": ":app",
                    "variantIdentity": "release"
                },
                "rules": {
                    "totalRules": 2,
                    "declarationCounts": {
                        "exact": 2,
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
                    }
                ],
                "diagnostics": [
                    {
                        "severity": "WARNING",
                        "message": "Rules 'token' and 'tok' overlap and remain order-sensitive.",
                        "affectedPhase": null,
                        "affectedPath": null
                    }
                ]
            }
            """),
        )
    }

    private fun givenProject(
        transformConfiguration: String,
        appMutator: File.(AndroidAppModule) -> Unit = {},
    ) {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.string-transform")
                    },
                    enableKotlinAndroidPlugin = false,
                    buildGradleExtra = transformConfiguration,
                    useKts = true,
                    mutator = appMutator,
                )
            ),
            useKts = true,
        ).generateIn(projectDir)
    }

    private fun assertSuccessfulReportText(
        report: String,
        pipeline: String,
        variant: String,
        totalRules: Int,
        exactDeclarations: Int,
        caseExpandedDeclarations: Int,
    ) {
        assertReportJsonMatches(
            report = report,
            expectedRegex = literalJsonPattern("""
            {
                "pipeline": "$pipeline",
                "variant": "$variant",
                "artifact": {
                    "moduleIdentity": ":app",
                    "variantIdentity": "$variant"
                },
                "rules": {
                    "totalRules": $totalRules,
                    "declarationCounts": {
                        "exact": $exactDeclarations,
                        "caseExpanded": $caseExpandedDeclarations
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
                    }
                ],
                "diagnostics": [
                    
                ]
            }
            """),
        )
    }

    private fun assertReportJsonMatches(report: String, expectedRegex: String) {
        assertThat(report.trim()).containsMatch(expectedRegex.trimIndent())
    }

    private fun literalJsonPattern(jsonBody: String): String {
        return "(?s)^\\Q${jsonBody.trimIndent()}\\E$"
    }
}
