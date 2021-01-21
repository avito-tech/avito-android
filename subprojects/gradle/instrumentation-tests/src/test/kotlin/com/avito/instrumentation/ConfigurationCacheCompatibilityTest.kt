package com.avito.instrumentation

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
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = listOf("com.avito.android.instrumentation-tests"),
                    buildGradleExtra = instrumentationConfiguration()
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
            "-PteamcityBuildId=0",
            "-PbuildNumber=100",
            "-PteamcityUrl=xxx",
            "-PgitBranch=xxx",
            "-PteamcityBuildType=BT",
            dryRun = true,
            configurationCache = true
        )
    }
}
