package com.avito.android.network_contracts.validation

import com.avito.android.network_contracts.NetworkCodegenProjectGenerator
import com.avito.android.network_contracts.codegen.CodegenTask
import com.avito.android.network_contracts.defaultModule
import com.avito.android.network_contracts.scheme.imports.data.models.SchemaEntry
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ValidateNetworkContractsTaskTest {

    @Test
    fun `when root validation task is invoked - then invoke modules validation report task with codegen`(
        @TempDir projectDir: File
    ) {
        val projectName = "feature"
        NetworkCodegenProjectGenerator.generate(projectDir, modules = listOf(defaultModule(name = projectName)))
        runTask(ValidateNetworkContractsRootTask.NAME, projectDir, dryRun = true)
            .assertThat()
            .tasksShouldBeTriggered(
                ":$projectName:${CodegenTask.NAME}",
                ":$projectName:${ValidateNetworkContractsSchemesTask.NAME}",
                ":${ValidateNetworkContractsRootTask.NAME}",
            )
    }

    @Test
    fun `when run validation task and schemes is empty -  then throw validation error`(
        @TempDir projectDir: File
    ) {
        val moduleName = "app"
        generateProjectWithGeneratedFiles(projectDir, emptyList(), schemes = emptyList(), moduleName = moduleName)
        runTask(ValidateNetworkContractsRootTask.NAME, projectDir, failed = true)
            .assertThat()
            .buildFailed()
            .outputContains("Module `:$moduleName` applies plugin, but does not contain any network contracts schemes.")
    }

    private fun generateProjectWithGeneratedFiles(
        projectDir: File,
        generatedFiles: List<File>,
        moduleName: String = "app",
        schemes: List<SchemaEntry> = listOf(
            SchemaEntry("test/path.yaml", "content")
        )
    ): List<File> {
        val packageName = "com.avito.android"

        val validateTaskExtraConfiguration = configureTestValidationTask()

        NetworkCodegenProjectGenerator.generate(
            projectDir,
            modules = listOf(
                defaultModule(
                    name = moduleName,
                    generatedClassesPackage = packageName,
                    buildExtra = validateTaskExtraConfiguration
                )
            )
        )
        NetworkCodegenProjectGenerator.generateSchemes(projectDir, schemes = schemes)
        return NetworkCodegenProjectGenerator.generateCodegenFiles(moduleName, projectDir, packageName, generatedFiles)
    }

    private fun runTask(
        name: String,
        tempDir: File,
        corruptFile: Boolean = false,
        failed: Boolean = false,
        dryRun: Boolean = false,
    ): TestResult {
        return gradlew(
            tempDir,
            ":$name", "-PcorruptFile=$corruptFile",
            expectFailure = failed,
            dryRun = dryRun,
            configurationCache = true,
            useTestFixturesClasspath = true
        )
    }

    private fun configureTestValidationTask(): String {
        return """
            tasks.named(
                "${CodegenTask.NAME}", 
                DefaultTask::class.java
            ).configure {
                isEnabled = false
            }
           
        """.trimIndent()
    }
}
