package com.avito.android.build_checks

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
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
                id("com.avito.android.build-checks")
            },
            buildGradleExtra = """
                 buildChecks {
                    androidSdk {
                        version(
                            compileSdkVersion = 30,
                            revision = 3
                        )
                    }
                    javaVersion {
                        version = org.gradle.api.JavaVersion.VERSION_11
                    }
                    preventKotlinDaemonFallback {
                        enabled = true
                    }
                 }
            """.trimIndent(),
            useKts = true
        ).generateIn(projectDir)

        runTask(projectDir).assertThat().buildSuccessful()

        runTask(projectDir).assertThat().buildSuccessful().configurationCachedReused()
    }

    private fun runTask(projectDir: File): TestResult {
        return gradlew(
            projectDir,
            "help",
            "-Pkapt.incremental.apt=true",
            "-Dos.name=Mac OS X",
            dryRun = false,
            configurationCache = true
        )
    }
}
