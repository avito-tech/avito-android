package com.avito.android.plugin

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ConfigurationCacheCompatibilityTest {

    @Test
    fun `configuration with applied plugin - ok`(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.room-config")
            },
            modules = listOf(
                AndroidLibModule(
                    name = "lib",
                    plugins = plugins {
                        id("kotlin-kapt")
                    }
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
            "-ParchPersistenceVersion=1.2.0",
            dryRun = true,
            configurationCache = true
        )
    }
}
