package com.avito.android.build_verdict

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
     * TODO: MBS-10424
     * 4 problems were found storing the configuration cache, 3 of which seem unique.
     *
     * Plugin 'com.avito.android.build-verdict':
     * registration of listener on 'Gradle.addListener' is unsupported
     * See https://docs.gradle.org/6.8/userguide/configuration_cache.html#config_cache:requirements:build_listeners
     *
     * Plugin 'com.avito.android.build-verdict':
     * registration of listener on 'Gradle.buildFinished' is unsupported
     * See https://docs.gradle.org/6.8/userguide/configuration_cache.html#config_cache:requirements:build_listeners
     *
     * Plugin 'com.avito.android.build-verdict':
     * registration of listener on 'Gradle.addBuildListener' is unsupported
     * See https://docs.gradle.org/6.8/userguide/configuration_cache.html#config_cache:requirements:build_listeners
     */
    @Disabled
    @Test
    fun `configuration with applied plugin`(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.build-verdict")
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
            "help",
            dryRun = true,
            configurationCache = true
        )
    }
}
