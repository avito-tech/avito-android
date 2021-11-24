package com.avito.android.info

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import java.util.Properties

class BuildPropertiesPluginTest {

    private lateinit var projectDir: File

    @BeforeEach
    fun setup(@TempDir tempPath: Path) {
        projectDir = tempPath.toFile()
    }

    @Test
    fun `assemble - creates build info properties`() {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    "app",
                    enableKotlinAndroidPlugin = false,
                    plugins = plugins {
                        id("com.avito.android.build-properties")
                    },
                    buildGradleExtra = """
                        buildProperties {
                            buildProperty("GIT_COMMIT", "commit")
                            buildProperty("BUILD_NUMBER", 1.toString())
                        }
                    """.trimIndent()
                )
            )
        ).generateIn(projectDir)

        gradlew(projectDir, "assemble")

        val properties = readProperties("app/src/main/assets/build-info.properties")

        assertThat(properties.getProperty("GIT_COMMIT")).isEqualTo("commit")
        assertThat(properties.getProperty("BUILD_NUMBER")).isEqualTo("1")
    }

    @Test
    fun `assemble - creates backward compatible build info properties`() {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    "app",
                    enableKotlinAndroidPlugin = false,
                    plugins = plugins {
                        id("com.avito.android.build-properties")
                    },
                    buildGradleExtra = """
                        buildInfo {
                            gitCommit = "commit"
                            gitBranch = "branch"
                            buildNumber = "build_number"
                        }
                    """.trimIndent()
                )
            )
        ).generateIn(projectDir)

        gradlew(projectDir, "assemble")

        val properties = readProperties("app/src/main/assets/app-build-info.properties")

        assertThat(properties.getProperty("GIT_COMMIT")).isEqualTo("commit")
        assertThat(properties.getProperty("GIT_BRANCH")).isEqualTo("branch")
        assertThat(properties.getProperty("BUILD_NUMBER")).isEqualTo("build_number")
    }

    private fun readProperties(path: String): Properties {
        val file = File(projectDir, path)

        assertThat(file.exists()).isTrue()
        assertThat(file.isFile).isTrue()

        return Properties().also { properties ->
            file.inputStream().use { input ->
                properties.load(input)
            }
        }
    }
}
