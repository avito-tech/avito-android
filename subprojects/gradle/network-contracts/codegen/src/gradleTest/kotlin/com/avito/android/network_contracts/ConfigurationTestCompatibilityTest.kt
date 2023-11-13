package com.avito.android.network_contracts

import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ConfigurationTestCompatibilityTest {

    @Test
    fun `configuration with applied plugin and addEndpoint task - ok`(@TempDir projectDir: File) {
        generateProject(projectDir)
        runAddEndpointTask(projectDir)
            .assertThat()
            .buildSuccessful()

        runAddEndpointTask(projectDir)
            .assertThat()
            .buildSuccessful()
            .configurationCachedReused()
    }

    @Test
    fun `configuration with applied plugin and codegen task - ok`(@TempDir projectDir: File) {
        generateProject(projectDir)
        runTask("codegen", projectDir)
            .assertThat()
            .buildSuccessful()

        runTask("codegen", projectDir)
            .assertThat()
            .buildSuccessful()
            .configurationCachedReused()
    }

    @Test
    fun `configuration with applied plugin and setup tmp mtls files task - ok`(@TempDir projectDir: File) {
        generateProject(projectDir)
        runTask("setupTmpMtlsFiles", projectDir)
            .assertThat()
            .buildSuccessful()

        runTask("setupTmpMtlsFiles", projectDir)
            .assertThat()
            .buildSuccessful()
            .configurationCachedReused()
    }

    @Test
    fun `configuration with applied plugin and make codegen files executable task - ok`(@TempDir projectDir: File) {
        generateProject(projectDir)
        runTask("makeFilesExecutable", projectDir)
            .assertThat()
            .buildSuccessful()

        runTask("makeFilesExecutable", projectDir)
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

    private fun runAddEndpointTask(
        tempDir: File,
    ): TestResult {
        return gradlew(
            tempDir,
            "addEndpoint", "-PapiSchemesUrl=",
            dryRun = true,
            configurationCache = true
        )
    }

    private fun runTask(
        name: String,
        tempDir: File,
    ): TestResult {
        return gradlew(
            tempDir,
            name,
            dryRun = true,
            configurationCache = true
        )
    }
}
