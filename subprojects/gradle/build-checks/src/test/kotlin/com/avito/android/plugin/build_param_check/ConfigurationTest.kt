package com.avito.android.plugin.build_param_check

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ConfigurationTest {

    @Test
    fun `disable by property - no side effects`(@TempDir projectDir: File) {
        val result = BuildChecksTestProjectRunner(
            projectDir,
            buildChecksExtension = """
                enableByDefault = true
                javaVersion {
                    version = JavaVersion.VERSION_1_1
                }
                androidSdk {
                    compileSdkVersion = -1
                    revision = -1
                }
            """
        ).runChecks(disablePlugin = true)

        result.assertThat().buildSuccessful()
        result.assertThat().tasksShouldNotBeTriggered("checkBuildEnvironment")
    }

    @Test
    fun `all checks disabled by default - no side effects`(@TempDir projectDir: File) {
        val result = BuildChecksTestProjectRunner(
            projectDir,
            buildChecksExtension = """
                enableByDefault = false
            """
        ).runChecks()

        result.assertThat().buildSuccessful()
    }

    @Test
    fun `all checks disabled explicitly - no side effects`(@TempDir projectDir: File) {
        val result = BuildChecksTestProjectRunner(
            projectDir,
            buildChecksExtension = """
                enableByDefault = true
                javaVersion { enabled = false }
                androidSdk { enabled = false }
                macOSLocalhost { enabled = false }
                dynamicDependencies { enabled = false }
                uniqueRClasses { enabled = false }
                gradleDaemon { enabled = false }
                moduleTypes { enabled = false }
                gradleProperties { enabled = false }
                incrementalKapt { enabled = false }
            """
        ).runChecks()

        result.assertThat().buildSuccessful()
    }

    @Test
    fun `custom project directory - no side effects`(@TempDir projectDir: File) {
        val result = BuildChecksTestProjectRunner(
            projectDir,
            buildChecksExtension = """
                enableByDefault = false
            """
        ).runChecks(startDir = "app")

        result.assertThat().buildSuccessful()
    }
}
