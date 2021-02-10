package com.avito.android.plugin

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ConfigurationCacheCompatibilityTest {

    /**
     * TODO
     *
     * Task `:app:generateTogglesReport` of type `com.avito.android.plugin.FeatureTogglesReportTask`:
     * cannot serialize object of type 'java.time.Ser', a subtype of 'java.io.Externalizable',
     * as these are not supported with the configuration cache.
     * See https://docs.gradle.org/6.8/userguide/configuration_cache.html#config_cache:not_yet_implemented:java_serialization
     */
    @Disabled
    @Test
    fun `configuration with applied plugin`(@TempDir projectDir: File) {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    "app",
                    plugins = plugins {
                        id("com.avito.android.feature-toggles")
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
            "generateTogglesReport",
            dryRun = true,
            configurationCache = true
        )
    }
}
