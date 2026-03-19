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
                        exact("source", "target")
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
        val reportText = reportFile.readText()
        assertThat(reportText).contains("\"pipeline\": \"alpha\"")
        assertThat(reportText).contains("\"variant\": \"release\"")
        assertThat(reportText).contains("\"artifact\"")
        assertThat(reportText).contains("\"rules\"")
        assertThat(reportText).contains("\"phases\"")
        assertThat(reportText).contains("\"diagnostics\"")
        assertThat(reportText).contains("\"moduleIdentity\": \":app\"")
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
                        exact("source", "target")
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
        assertThat(reportFile.readText()).contains("\"variant\": \"paidRelease\"")
    }

    @Test
    fun `plugin report - writes rule summary - when exact and case-expanded rules are declared`() {
        givenProject(
            """
            transformStrings {
                create("alpha") {
                    variant("release")
                    rules {
                        exact("source", "target")
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
        val reportText = reportFile.readText()
        assertThat(reportText).contains("\"totalRules\": 4")
        assertThat(reportText).contains("\"declarationCounts\"")
        assertThat(reportText).contains("\"exact\": 1")
        assertThat(reportText).contains("\"caseExpanded\": 1")
    }

    @Test
    fun `plugin report - uses final pipeline configuration - when rules are added after create call`() {
        givenProject(
            """
            transformStrings {
                val pipeline = create("alpha") {
                    variant("release")
                    rules {
                        exact("source", "target")
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
        val reportText = reportFile.readText()
        assertThat(reportText).contains("\"totalRules\": 3")
        assertThat(reportText).contains("\"declarationCounts\"")
        assertThat(reportText).contains("\"exact\": 1")
        assertThat(reportText).contains("\"caseExpanded\": 1")
    }

    @Test
    fun `plugin tasks - use final pipeline configuration - when variant is set after create call`() {
        givenProject(
            """
            transformStrings {
                val pipeline = create("alpha") {
                    rules {
                        exact("source", "target")
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
        assertThat(reportFile.readText()).contains("\"variant\": \"release\"")
    }

    @Test
    fun `plugin root task - executes all matched variant tasks - when multiple pipelines target the same variant`() {
        givenProject(
            """
            transformStrings {
                create("alpha") {
                    variant("release")
                    rules {
                        exact("source", "target")
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
        assertThat(alphaReport.readText()).contains("\"pipeline\": \"alpha\"")
        assertThat(betaReport.readText()).contains("\"pipeline\": \"beta\"")
    }

    @Test
    fun `plugin configuration cache - reuses entry - when configuration is unchanged`() {
        givenProject(
            """
            transformStrings {
                create("alpha") {
                    variant("release")
                    rules {
                        exact("source", "target")
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
                        exact("source", "target")
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
                        exact("source", "target")
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
                        exact("source", "target")
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
                        exact("source", "target")
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
                        exact("source", "target")
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
        assertThat(reportFile.readText()).contains("\"severity\": \"WARNING\"")
        assertThat(reportFile.readText()).contains("overlap and remain order-sensitive")
    }

    private fun givenProject(transformConfiguration: String) {
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
                )
            ),
            useKts = true,
        ).generateIn(projectDir)
    }
}
