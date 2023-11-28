package com.avito.android.network_contracts.scheme.imports

import com.avito.android.network_contracts.NetworkCodegenProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ConfigurationTestCompatibilityTest {

    @Test
    fun `configuration with applied plugin and addEndpoint - ok`(@TempDir projectDir: File) {
        generateProject(projectDir)
        runTask(projectDir)
            .assertThat()
            .buildSuccessful()

        runTask(projectDir)
            .assertThat()
            .buildSuccessful()
            .configurationCachedReused()
    }

    private fun generateProject(
        projectDir: File
    ) {
        NetworkCodegenProjectGenerator.generate(
            projectDir = projectDir,
            serviceUrl = "/",
        )
    }

    private fun runTask(
        tempDir: File,
    ): TestResult {
        return gradlew(
            tempDir,
            "addEndpoint", "-PapiSchemesUrl=",
            dryRun = true,
            configurationCache = true
        )
    }
}
