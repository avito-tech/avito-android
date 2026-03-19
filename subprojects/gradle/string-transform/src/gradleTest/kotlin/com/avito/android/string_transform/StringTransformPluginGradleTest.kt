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
    fun `plugin tasks - register variant task and metadata - when exact release variant matches`() {
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

        val metadataFile = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/release/metadata/pipeline-variant-metadata.json"
        )
        assertThat(metadataFile.exists()).isTrue()
        val metadataText = metadataFile.readText()
        assertThat(metadataText).contains("\"pipeline\": \"alpha\"")
        assertThat(metadataText).contains("\"variant\": \"release\"")
    }

    @Test
    fun `plugin tasks - register variant task and metadata - when exact flavored variant matches`() {
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

        val metadataFile = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/paidRelease/metadata/pipeline-variant-metadata.json"
        )
        assertThat(metadataFile.exists()).isTrue()
        assertThat(metadataFile.readText()).contains("\"variant\": \"paidRelease\"")
    }

    @Test
    fun `plugin metadata - writes normalized rules - when exact and case-expanded rules are declared`() {
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

        val metadataFile = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/release/metadata/pipeline-variant-metadata.json"
        )

        assertThat(metadataFile.exists()).isTrue()
        val metadataText = metadataFile.readText()
        assertThat(metadataText).contains("\"outputRelativePath\": \"outputs/transformStrings/alpha/release\"")
        assertThat(metadataText).contains("\"from\": \"source\"")
        assertThat(metadataText).contains("\"to\": \"target\"")
        assertThat(metadataText).contains("\"from\": \"token\"")
        assertThat(metadataText).contains("\"to\": \"value\"")
        assertThat(metadataText).contains("\"from\": \"TOKEN\"")
        assertThat(metadataText).contains("\"to\": \"VALUE\"")
        assertThat(metadataText).contains("\"from\": \"Token\"")
        assertThat(metadataText).contains("\"to\": \"Value\"")
    }

    @Test
    fun `plugin metadata - uses final pipeline configuration - when rules are added after create call`() {
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

        val metadataFile = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/release/metadata/pipeline-variant-metadata.json"
        )

        assertThat(metadataFile.exists()).isTrue()
        val metadataText = metadataFile.readText()
        assertThat(metadataText).contains("\"from\": \"source\"")
        assertThat(metadataText).contains("\"to\": \"target\"")
        assertThat(metadataText).contains("\"from\": \"token\"")
        assertThat(metadataText).contains("\"to\": \"value\"")
        assertThat(metadataText).doesNotContain("\"from\": \"TOKEN\"")
        assertThat(metadataText).doesNotContain("\"to\": \"VALUE\"")
        assertThat(metadataText).contains("\"from\": \"Token\"")
        assertThat(metadataText).contains("\"to\": \"Value\"")
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

        val metadataFile = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/release/metadata/pipeline-variant-metadata.json"
        )

        assertThat(metadataFile.exists()).isTrue()
        assertThat(metadataFile.readText()).contains("\"variant\": \"release\"")
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

        val alphaMetadata = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/release/metadata/pipeline-variant-metadata.json"
        )
        val betaMetadata = File(
            projectDir,
            "app/build/outputs/transformStrings/beta/release/metadata/pipeline-variant-metadata.json"
        )

        assertThat(alphaMetadata.exists()).isTrue()
        assertThat(betaMetadata.exists()).isTrue()
        assertThat(betaMetadata.readText()).contains("\"from\": \"token\"")
        assertThat(betaMetadata.readText()).contains("\"to\": \"value\"")
        assertThat(betaMetadata.readText()).doesNotContain("\"from\": \"TOKEN\"")
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
    fun `plugin warnings - print warning - when matched pipeline contains overlapping rules`() {
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

        val output = gradlew(
            projectDir,
            ":app:help",
            useTestFixturesClasspath = true,
        ).output

        assertThat(output).contains("overlap and remain order-sensitive")
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
