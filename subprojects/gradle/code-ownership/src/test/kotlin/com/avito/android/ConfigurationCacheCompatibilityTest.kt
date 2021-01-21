package com.avito.android

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidLibModule
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ConfigurationCacheCompatibilityTest {

    @Test
    fun `configuration with applied plugin - ok`(@TempDir projectDir: File) {
        TestProjectGenerator(
            name = "rootapp",
            plugins = listOf("com.avito.android.code-ownership"),
            modules = listOf(
                AndroidLibModule(name = "lib")
            )
        ).generateIn(projectDir)

        runTask(projectDir).assertThat().buildSuccessful()

        runTask(projectDir).assertThat().buildSuccessful().configurationCachedReused()
    }

    private fun runTask(tempDir: File): TestResult {
        return gradlew(
            tempDir,
            "checkProjectDependenciesOwnership",
            "-Pavito.moduleOwnershipValidationEnabled",
            dryRun = true,
            configurationCache = true
        )
    }
}
