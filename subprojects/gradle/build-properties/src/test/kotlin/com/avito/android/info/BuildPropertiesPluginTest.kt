package com.avito.android.info

import com.avito.test.gradle.AndroidAppModule
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.git
import com.avito.test.gradle.gradlew
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
                    plugins = listOf("com.avito.android.build-properties"),
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

        with(projectDir) {
            git("checkout -b develop")
        }

        gradlew(projectDir, "assemble")
        
        val propertiesFile = File(projectDir, "app/src/main/assets/app-build-info.properties")

        assertThat(propertiesFile.exists()).isTrue()
        assertThat(propertiesFile.isFile).isTrue()

        propertiesFile.inputStream().use { input ->
            val properties = Properties()
            properties.load(input)

            assertThat(properties.getProperty("GIT_COMMIT")).isEqualTo("commit")
            assertThat(properties.getProperty("GIT_BRANCH")).isEqualTo("branch")
            assertThat(properties.getProperty("BUILD_NUMBER")).isEqualTo("build_number")
        }
    }
}
