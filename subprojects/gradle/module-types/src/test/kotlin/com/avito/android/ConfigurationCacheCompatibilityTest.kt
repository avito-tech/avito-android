package com.avito.android

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ConfigurationCacheCompatibilityTest {

    @Test
    fun `configuration with applied plugin - ok`(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = listOf("com.avito.android.impact"),
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = listOf("com.avito.android.module-types")
                )
            )
        ).generateIn(projectDir)

        runTask(projectDir).assertThat().buildSuccessful()

        runTask(projectDir).assertThat().buildSuccessful().configurationCachedReused()
    }

    private fun runTask(projectDir: File): TestResult {
        return gradlew(
            projectDir,
            "help",
            "-Pavito.moduleTypeValidationEnabled=true",
            "-PgitBranch=xxx", // todo need for impact plugin
            dryRun = true,
            configurationCache = true
        )
    }
}
