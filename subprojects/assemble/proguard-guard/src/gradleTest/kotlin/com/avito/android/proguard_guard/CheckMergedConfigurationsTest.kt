package com.avito.android.proguard_guard

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class CheckMergedConfigurationsTest {

    @field:TempDir
    private lateinit var projectDir: File

    @Test
    fun `same configuration - success`() {
        generateProject(
            buildGradleExtra = """
                $ENABLE_MINIFICATION
                proguardGuard {
                    lockVariant("debug", file("$TEST_MERGED_CONFIG_PATH"))
                }
                """.trimIndent(),
            configFilesCreator = {
                file(TEST_MERGED_CONFIG_PATH, TEST_CONFIG_CONTENT)
                file(DEFAULT_LOCKED_CONFIG_PATH, TEST_CONFIG_CONTENT)
            }
        )

        val build = gradlew(projectDir, "checkDebugMergedProguard")

        build.assertThat().buildSuccessful()
        assertThat(projectDir.resolve(DEFAULT_OUTPUT_PATH).exists()).isFalse()
    }

    @Test
    fun `same configuration, different comments - success`() {
        generateProject(
            buildGradleExtra = """
                $ENABLE_MINIFICATION
                proguardGuard {
                    lockVariant("debug", file("$TEST_MERGED_CONFIG_PATH"))
                }
                """.trimIndent(),
            configFilesCreator = {
                file(
                    TEST_MERGED_CONFIG_PATH,
                    content = "#comment1\n$TEST_CONFIG_CONTENT"
                )
                file(
                    DEFAULT_LOCKED_CONFIG_PATH,
                    content = "#comment2\n\n$TEST_CONFIG_CONTENT"
                )
            }
        )

        val build = gradlew(projectDir, "checkDebugMergedProguard")

        build.assertThat().buildSuccessful()
        assertThat(projectDir.resolve(DEFAULT_OUTPUT_PATH).exists()).isFalse()
    }

    @Test
    fun `different configuration - fail`() {
        generateProject(
            buildGradleExtra = """
                $ENABLE_MINIFICATION
                proguardGuard {
                    lockVariant("debug", file("$TEST_MERGED_CONFIG_PATH"))
                }
                """.trimIndent(),
            configFilesCreator = {
                file(
                    TEST_MERGED_CONFIG_PATH,
                    content = """
                        -optimizationpasses 5
                        -allowaccessmodification
                        """.trimIndent(),
                )
                file(
                    DEFAULT_LOCKED_CONFIG_PATH,
                    content = """
                        -optimizationpasses 4
                        -allowaccessmodification
                        """.trimIndent()
                )
            }
        )

        val build = gradlew(projectDir, "checkDebugMergedProguard", expectFailure = true)
        val output = projectDir.resolve(DEFAULT_OUTPUT_PATH)

        build.assertThat().buildFailed()
        assertThat(output.exists()).isTrue()
        val diffContent = output.readText()
        assertThat(diffContent).contains("--- -optimizationpasses 4")
        assertThat(diffContent).contains("+++ -optimizationpasses 5")
    }

    @Test
    fun `custom locked config path - success`() {
        val lockedPath = "myconfigs/locked.pro"
        generateProject(
            buildGradleExtra = """
                proguardGuard {
                    $ENABLE_MINIFICATION
                    lockVariant("debug", file("$TEST_MERGED_CONFIG_PATH")) {
                        lockedConfigurationFile = file("$lockedPath")
                    }
                }
                """.trimIndent(),
            configFilesCreator = {
                file(
                    TEST_MERGED_CONFIG_PATH,
                    content = TEST_CONFIG_CONTENT
                )
                file(
                    lockedPath,
                    content = TEST_CONFIG_CONTENT
                )
            }
        )

        val build = gradlew(projectDir, "checkDebugMergedProguard")

        build.assertThat().buildSuccessful()
        assertThat(projectDir.resolve(DEFAULT_OUTPUT_PATH).exists()).isFalse()
    }

    @Test
    fun `custom output path - fail with custom diff path`() {
        val outputPath = "outputs/diff.txt"
        generateProject(
            buildGradleExtra = """
                $ENABLE_MINIFICATION
                proguardGuard {
                    lockVariant("debug", file("$TEST_MERGED_CONFIG_PATH")) {
                        outputFile = file("$outputPath")
                    }
                }
                """.trimIndent(),
            configFilesCreator = {
                file(
                    TEST_MERGED_CONFIG_PATH,
                    content = "-allowaccessmodification"
                )
                file(
                    DEFAULT_LOCKED_CONFIG_PATH,
                    content = ""
                )
            }
        )

        val build = gradlew(projectDir, "checkDebugMergedProguard", expectFailure = true)

        build.assertThat().buildFailed()
        assertThat(projectDir.resolve(DEFAULT_OUTPUT_PATH).exists()).isFalse()
        assertThat(projectDir.resolve("$MODULE_NAME/$outputPath").exists()).isTrue()
    }

    @Test
    fun `merged file not exists - fail`() {
        generateProject(
            buildGradleExtra = """
                $ENABLE_MINIFICATION
                proguardGuard {
                    lockVariant("debug", file("$TEST_MERGED_CONFIG_PATH"))
                }
                """.trimIndent(),
            configFilesCreator = { }
        )

        val build = gradlew(projectDir, "checkDebugMergedProguard", expectFailure = true)

        build.assertThat().buildFailed()
        build.assertThat().outputContains("doesn't exist")
    }

    @Test
    fun `don't fail on difference - success with diff file`() {
        generateProject(
            buildGradleExtra = """
                $ENABLE_MINIFICATION
                proguardGuard {
                    lockVariant("debug", file("$TEST_MERGED_CONFIG_PATH")) {
                        failOnDifference = false
                    }
                }
                """.trimIndent(),
            configFilesCreator = {
                file(
                    TEST_MERGED_CONFIG_PATH,
                    content = "-allowaccessmodification"
                )
                file(
                    DEFAULT_LOCKED_CONFIG_PATH,
                    content = ""
                )
            }
        )

        val build = gradlew(projectDir, "checkDebugMergedProguard")

        build.assertThat().buildSuccessful()
        assertThat(projectDir.resolve(DEFAULT_OUTPUT_PATH).exists()).isTrue()
    }

    @Test
    fun `locked config doesn't exists - success with new locked file`() {
        generateProject(
            buildGradleExtra = """
                $ENABLE_MINIFICATION
                proguardGuard {
                    lockVariant("debug", file("$TEST_MERGED_CONFIG_PATH"))
                }
                """.trimIndent(),
            configFilesCreator = {
                file(
                    TEST_MERGED_CONFIG_PATH,
                    content = TEST_CONFIG_CONTENT
                )
            }
        )

        val build = gradlew(projectDir, "checkDebugMergedProguard")
        val locked = projectDir.resolve("$MODULE_NAME/$DEFAULT_LOCKED_CONFIG_PATH")

        build.assertThat().buildSuccessful()
        assertThat(locked.exists()).isTrue()
        assertThat(locked.readText()).isEqualTo(TEST_CONFIG_CONTENT)
    }

    @Test
    fun `minify is disabled - fail`() {
        generateProject(
            buildGradleExtra = """
                proguardGuard {
                    lockVariant("debug", file("$TEST_MERGED_CONFIG_PATH"))
                }
                """.trimIndent(),
            configFilesCreator = {
                file(TEST_MERGED_CONFIG_PATH, TEST_CONFIG_CONTENT)
                file(DEFAULT_LOCKED_CONFIG_PATH, TEST_CONFIG_CONTENT)
            }
        )

        val build = gradlew(projectDir, "checkDebugMergedProguard", expectFailure = true)

        build.assertThat().buildFailed()
        build.assertThat().outputContains(
            "Task minifyDebugWithR8 was not found in project :app. " +
                "You probably forgot to set minifyEnabled to true."
        )
    }

    private fun generateProject(
        buildGradleExtra: String,
        configFilesCreator: File.(AndroidAppModule) -> Unit
    ) {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = MODULE_NAME,
                    enableKotlinAndroidPlugin = false,
                    plugins = plugins {
                        id("com.avito.android.proguard-guard")
                    },
                    buildGradleExtra = buildGradleExtra,
                    mutator = configFilesCreator,
                )
            ),
        ).generateIn(projectDir)
    }

    companion object {
        const val MODULE_NAME = "app"
        const val DEFAULT_LOCKED_CONFIG_PATH = "proguard-guard/debug/locked-configuration.pro"
        const val DEFAULT_OUTPUT_PATH = "$MODULE_NAME/build/outputs/proguard_guard/debug/diff.txt"
        const val TEST_MERGED_CONFIG_PATH = "merged-config.pro"
        const val TEST_CONFIG_CONTENT = "-optimizationpasses 5\n-allowaccessmodification"
        const val ENABLE_MINIFICATION = "android.buildTypes.debug.minifyEnabled = true"
    }
}
