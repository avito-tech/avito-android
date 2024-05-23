package com.avito.android.network_contracts

import com.avito.android.network_contracts.scheme.fixation.collect.CollectApiSchemesTask
import com.avito.android.network_contracts.scheme.fixation.upsert.UpdateRemoteApiSchemesTask
import com.avito.android.network_contracts.scheme.imports.ApiSchemesImportTask
import com.avito.android.network_contracts.validation.ValidateNetworkContractsRootTask
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.Module
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ConfigurationTestCompatibilityTest {

    @Test
    fun `configuration with applied plugin and addEndpoint task - ok`(@TempDir projectDir: File) {
        checkConfigurationCacheCompatibility(projectDir, ApiSchemesImportTask.NAME, "-PapiSchemesUrl=")
    }

    @Test
    fun `configuration with applied plugin and codegen task - ok`(@TempDir projectDir: File) {
        checkConfigurationCacheCompatibility(projectDir, "codegen")
    }

    @Test
    fun `configuration with applied plugin and setup tmp mtls files task - ok`(@TempDir projectDir: File) {
        checkConfigurationCacheCompatibility(projectDir, "setupTmpMtlsFiles")
    }

    @Test
    fun `configuration with applied plugin and make codegen files executable task - ok`(@TempDir projectDir: File) {
        checkConfigurationCacheCompatibility(projectDir, "makeFilesExecutable")
    }

    @Test
    fun `configuration with applied plugin and collect api schemes task - ok`(@TempDir projectDir: File) {
        checkConfigurationCacheCompatibility(projectDir, CollectApiSchemesTask.NAME)
    }

    @Test
    fun `configuration with applied plugin and upsert contracts task - ok`(@TempDir projectDir: File) {
        checkConfigurationCacheCompatibility(
            projectDir = projectDir,
            taskName = UpdateRemoteApiSchemesTask.NAME,
            "-Pavito.networkContracts.fixation.author="
        )
    }

    @Test
    fun `configuration with applied plugin and contracts validation task - ok`(@TempDir projectDir: File) {
        checkConfigurationCacheCompatibility(projectDir, ValidateNetworkContractsRootTask.NAME)
    }

    @Test
    fun `configuration with applied plugin and compile task - ok`(@TempDir projectDir: File) {
        checkConfigurationCacheCompatibility(projectDir, "compileKotlin")
    }

    private fun checkConfigurationCacheCompatibility(
        projectDir: File,
        taskName: String,
        vararg args: String,
    ) {
        checkConfigurationCacheCompatibility(
            projectDir, listOf(defaultModule()), taskName, *args
        )
    }

    private fun checkConfigurationCacheCompatibility(
        projectDir: File,
        modules: List<Module>,
        taskName: String,
        vararg args: String,
    ) {
        generateProject(projectDir, modules)
        runTask(taskName, projectDir, *args)
            .assertThat()
            .buildSuccessful()

        runTask(taskName, projectDir, *args)
            .assertThat()
            .buildSuccessful()
            .configurationCachedReused()
    }

    private fun generateProject(
        projectDir: File,
        modules: List<Module>,
    ) {
        NetworkCodegenProjectGenerator.generate(
            projectDir = projectDir,
            serviceUrl = "/",
            modules = modules,
        )
    }

    private fun runTask(
        name: String,
        tempDir: File,
        vararg args: String,
    ): TestResult {
        return gradlew(
            tempDir,
            name, *args,
            dryRun = true,
            configurationCache = true
        )
    }
}
