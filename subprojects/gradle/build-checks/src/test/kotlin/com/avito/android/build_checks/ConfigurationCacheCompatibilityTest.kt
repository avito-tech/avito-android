package com.avito.android.build_checks

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ConfigurationCacheCompatibilityTest {

    /**
     * TODO blocked by agp 4.2
     */
    @Disabled
    @Test
    fun `configuration with applied plugin - ok`(@TempDir projectDir: File) {
        TestProjectGenerator(
            name = "rootapp",
            plugins = plugins {
                id("com.avito.android.buildchecks")
            },
            buildGradleExtra = """
                 buildChecks {
                     enableByDefault = false
                 }
            """.trimIndent()
        ).generateIn(projectDir)

        runTask(projectDir).assertThat().buildSuccessful()

        runTask(projectDir).assertThat().buildSuccessful().configurationCachedReused()
    }

    private fun runTask(projectDir: File): TestResult {
        return gradlew(
            projectDir,
            "help",
            dryRun = true,
            configurationCache = true
        )
    }
}
