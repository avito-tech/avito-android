package com.avito.android.plugin.build_param_check

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ConfigurationTest {

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
}
