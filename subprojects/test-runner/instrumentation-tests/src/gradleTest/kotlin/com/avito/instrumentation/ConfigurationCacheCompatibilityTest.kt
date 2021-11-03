package com.avito.instrumentation

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
    fun `configuration with applied plugin - reuses configuration cache`(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.gradle-logger")
            },
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id(instrumentationPluginId)
                    },
                    buildGradleExtra = instrumentationConfiguration()
                )
            )
        ).generateIn(projectDir)

        runHelp(projectDir).assertThat().buildSuccessful()

        runHelp(projectDir).assertThat().buildSuccessful().configurationCachedReused()
    }

    @Test
    fun `instrumentationTask run - reuses configuration cache`(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.gradle-logger")
            },
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id(instrumentationPluginId)
                    },
                    buildGradleExtra = instrumentationConfiguration()
                )
            )
        ).generateIn(projectDir)

        runTask(projectDir).assertThat().buildSuccessful()

        runTask(projectDir).assertThat().buildSuccessful().configurationCachedReused()
    }

    private fun runHelp(projectDir: File): TestResult {
        return gradlew(
            projectDir,
            "help",
            "-PteamcityBuildId=0",
            "-PbuildNumber=100",
            "-PteamcityUrl=xxx",
            "-PgitBranch=xxx",
            "-PteamcityBuildType=BT",
            dryRun = true,
            configurationCache = true
        )
    }

    private fun runTask(projectDir: File): TestResult {
        return gradlew(
            projectDir,
            ":app:instrumentationFunctionalLocal",
            "-PteamcityBuildId=0",
            "-PbuildNumber=100",
            "-PteamcityUrl=xxx",
            "-PgitBranch=xxx",
            "-PteamcityBuildType=BT",
            "-PisGradleTestKitRun=true",
            dryRun = true,
            configurationCache = true
        )
    }
}
