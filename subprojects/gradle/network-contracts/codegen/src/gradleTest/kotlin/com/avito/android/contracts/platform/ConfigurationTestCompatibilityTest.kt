package com.avito.android.contracts.platform

import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.Module
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ConfigurationTestCompatibilityTest {

    @Test
    fun `configuration with applied plugin and addEndpoint task - ok`(@TempDir projectDir: File) {
        checkConfigurationCacheCompatibility(
            projectDir,
            ContractsTaskNamesBuilder.importSchemeTask("test"),
            "-PapiSchemesUrl="
        )
    }

    @Test
    fun `configuration with applied plugin and codegen task - ok`(@TempDir projectDir: File) {
        checkConfigurationCacheCompatibility(projectDir, ContractsTaskNamesBuilder.codegenTask())
    }

    @Test
    fun `configuration with applied plugin and setup tmp mtls files task - ok`(@TempDir projectDir: File) {
        checkConfigurationCacheCompatibility(projectDir, "setupTmpMtlsFiles")
    }

    @Test
    fun `configuration with applied plugin and collect api schemes task - ok`(@TempDir projectDir: File) {
        checkConfigurationCacheCompatibility(projectDir, ContractsTaskNamesBuilder.collectSchemesTask("test"))
    }

    @Test
    fun `configuration with applied plugin and upsert contracts task - ok`(@TempDir projectDir: File) {
        checkConfigurationCacheCompatibility(
            projectDir = projectDir,
            taskName = ContractsTaskNamesBuilder.updateSchemesTask("all"),
            "-Pavito.networkContracts.fixation.author="
        )
    }

    @Test
    fun `configuration with applied plugin and contracts validation task - ok`(@TempDir projectDir: File) {
        checkConfigurationCacheCompatibility(projectDir, ContractsTaskNamesBuilder.validationTask("test", "all"))
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
        generateProject(projectDir) { listOf(defaultModule(variants = it)) }
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
        modules: (List<NetworkCodegenProjectGenerator.Variant>) -> List<Module>,
    ) {
        NetworkCodegenProjectGenerator.generate(
            projectDir = projectDir,
            modules = modules,
        )
    }

    private fun runTask(
        name: String,
        tempDir: File,
        vararg args: String,
    ): TestResult {
        val runArgs = arrayOf(*args) + arrayOf("-Pavito.clickstream.serviceUrl=stub")
        return gradlew(
            tempDir,
            name, *runArgs,
            dryRun = true,
            configurationCache = true
        )
    }
}
