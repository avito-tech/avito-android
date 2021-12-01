package com.avito.android.plugin.build_metrics

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
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
            },
            modules = listOf(
                AndroidAppModule(name = "app")
            )
        ).generateIn(projectDir)

        runTask(projectDir).assertThat().buildSuccessful()

        runTask(projectDir).assertThat().buildSuccessful().configurationCachedReused()
    }

    private fun runTask(projectDir: File): TestResult {
        return gradlew(
            projectDir,
            ":app:preBuild",
            "-Pavito.build.metrics.enabled=true",
            dryRun = false,
            configurationCache = true
        )
    }
}
