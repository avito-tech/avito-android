package com.avito.ci.steps

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.ciRun
import com.avito.test.gradle.module.PlatformModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

internal class DependencyAnalysisStepTest {

    @Test
    fun `dependencyAnalysis - triggers projectHealth in modules`(@TempDir tempDir: Path) {
        val projectDir = tempDir.toFile()

        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.cd")
                id("com.autonomousapps.dependency-analysis").version("0.74.0")
            },
            modules = listOf(
                // Don't use arbitrary module due to issue with Kotlin. Didn't find workaround.
                // https://github.com/autonomousapps/dependency-analysis-android-gradle-plugin/issues/432
                PlatformModule(
                    name = "module"
                )
            ),
            buildGradleExtra = """
                builds {
                    release {
                        dependencyAnalysis {}
                    }
                }
            """.trimIndent(),
        ).generateIn(projectDir)

        val result = runTask(projectDir, "release")

        result.assertThat()
            .buildSuccessful()
            .tasksShouldBeTriggered(":module:projectHealth")
    }

    private fun runTask(projectDir: File, taskName: String): TestResult =
        ciRun(projectDir, taskName, dryRun = true)
}
