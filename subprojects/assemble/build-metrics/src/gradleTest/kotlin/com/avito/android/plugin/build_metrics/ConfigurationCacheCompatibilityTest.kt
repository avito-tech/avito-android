package com.avito.android.plugin.build_metrics

import com.avito.git.Git
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ConfigurationCacheCompatibilityTest {

    @Test
    fun `configuration with applied plugin - ok`(@TempDir projectDir: File) {
        TestProjectGenerator(
            name = "rootapp",
            plugins = plugins {
                id("com.avito.android.build-metrics")
                id("com.avito.android.gradle-logger")
            },
            imports = listOf(
                "import com.avito.android.plugin.build_metrics.BuildEnvironment",
            ),
            buildGradleExtra = """
                |buildMetrics {
                |   buildType.set("test")
                |   environment.set(BuildEnvironment.CI)
                |}
            """.trimMargin(),
            modules = listOf(
                AndroidAppModule(name = "app")
            )
        ).generateIn(projectDir)

        runTask(projectDir).assertThat().buildSuccessful()

        runTask(projectDir).assertThat().buildSuccessful().configurationCachedReused()
    }

    /**
     * Verifies the goal of MBSA-2353: a `git commit` between two builds does not
     * invalidate the configuration cache.
     */
    @Test
    fun `git commit between builds - CC reused`(@TempDir projectDir: File) {
        TestProjectGenerator(
            name = "rootapp",
            plugins = plugins {
                id("com.avito.android.build-metrics")
                id("com.avito.android.gradle-logger")
            },
            imports = listOf(
                "import com.avito.android.plugin.build_metrics.BuildEnvironment",
            ),
            buildGradleExtra = """
                |buildMetrics {
                |   buildType.set("test")
                |   environment.set(BuildEnvironment.CI)
                |}
            """.trimMargin(),
            modules = listOf(
                AndroidAppModule(name = "app")
            )
        ).generateIn(projectDir)

        val notesFile = File(projectDir, "notes.txt").apply { writeText("v1") }

        val git = Git.create(projectDir)
        git.init().getOrThrow()
        git.checkout(branchName = "feature", create = true).getOrThrow()
        git.addAll().getOrThrow()
        git.commit("initial").getOrThrow()

        runTask(projectDir).assertThat().buildSuccessful()

        notesFile.writeText("v2")
        git.commit("between builds").getOrThrow()

        runTask(projectDir).assertThat().buildSuccessful().configurationCachedReused()
    }

    private fun runTask(projectDir: File): TestResult {
        return BuildMetricsRunner(projectDir)
            .build(
                listOf(":app:preBuild")
            )
    }
}
