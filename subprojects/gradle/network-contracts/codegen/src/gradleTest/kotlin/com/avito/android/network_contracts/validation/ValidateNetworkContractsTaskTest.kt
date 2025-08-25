@file:Suppress("MaxLineLength")

package com.avito.android.network_contracts.validation

import com.avito.android.network_contracts.NetworkCodegenProjectGenerator
import com.avito.android.network_contracts.codegen.CodegenTask
import com.avito.android.network_contracts.codegen.SetupTmpMtlsFilesTask
import com.avito.android.network_contracts.defaultModule
import com.avito.android.network_contracts.scheme.imports.data.models.SchemaEntry
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.dir
import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.io.File
import java.util.stream.Stream

class ValidateNetworkContractsTaskTest {

    @Test
    fun `when root validation task is invoked - validationByCodegen is true - then invoke modules validation report task with codegen`(
        @TempDir projectDir: File
    ) {
        val projectName = "feature"
        generateProjectWithGeneratedFiles(projectDir, emptyList(), moduleName = projectName, validationByCodegen = true)
        val assert = runTask(ValidateNetworkContractsTask.NAME, projectDir, dryRun = true)
            .assertThat()

        assert.tasksShouldBeTriggered(
            ":${SetupTmpMtlsFilesTask.NAME}",
            ":$projectName:${CodegenTask.NAME}Validate",
            ":$projectName:validateNetworkContractsFiles",
            ":$projectName:${ValidateNetworkContractsTask.NAME}",
            ":${ValidateNetworkContractsTask.NAME}",
        )
            .inOrder()

        runTask(ValidateNetworkContractsTask.NAME, projectDir)
            .assertThat()
            .taskWithOutcome(":$projectName:validateNetworkContractsByRemote", TaskOutcome.SKIPPED)

        assert.tasksShouldNotBeTriggered(":$projectName:collectApiSchemes")
    }

    @Test
    fun `when root validation task is invoked - validationByCodegen is false - then invoke modules validation report task with codegen`(
        @TempDir projectDir: File
    ) {
        val projectName = "feature"
        generateProjectWithGeneratedFiles(projectDir, emptyList(), moduleName = projectName, validationByCodegen = false)
        val assert = runTask(ValidateNetworkContractsTask.NAME, projectDir, dryRun = true)
            .assertThat()

        assert
            .tasksShouldBeTriggered(
                ":$projectName:collectApiSchemes",
                ":$projectName:validateNetworkContractsFiles",
                ":$projectName:validateNetworkContractsByRemote",
                ":$projectName:${ValidateNetworkContractsTask.NAME}",
                ":${ValidateNetworkContractsTask.NAME}",
            )
            .inOrder()

        assert.tasksShouldNotBeTriggered("${CodegenTask.NAME}Validate")
    }

    @ParameterizedTest
    @ArgumentsSource(ValidationByCodegenArgumentSource::class)
    fun `when module validation task is invoked and failFast is enable - then invoke module validation task with codegen - fail task with report`(
        validationByCodegen: Boolean,
        @TempDir projectDir: File
    ) {
        val moduleName = "app"
        generateProjectWithGeneratedFiles(
            projectDir,
            emptyList(),
            schemes = emptyList(),
            moduleName = moduleName,
            validationByCodegen = validationByCodegen
        )
        runTask(ValidateNetworkContractsTask.NAME, projectDir, failed = true)
            .assertThat()
            .buildFailed()
            .outputContains("Module `:$moduleName` applies plugin, but does not contain any network contracts schemes.")
    }

    @ParameterizedTest
    @ArgumentsSource(ValidationByCodegenArgumentSource::class)
    fun `when run validation task and schemes is empty -  then throw validation error`(
        validationByCodegen: Boolean,
        @TempDir projectDir: File
    ) {
        val moduleName = "app"
        generateProjectWithGeneratedFiles(
            projectDir,
            generatedFiles = emptyList(),
            schemes = emptyList(),
            moduleName = moduleName,
            failFast = true,
            validationByCodegen = validationByCodegen
        )
        runTask("$moduleName:${ValidateNetworkContractsTask.NAME}", projectDir, failed = true)
            .assertThat()
            .buildFailed()
            .outputContains("Module `:$moduleName` applies plugin, but does not contain any network contracts schemes.")
            .apply {
                tasksShouldBeTriggered(":$moduleName:validateNetworkContractsFiles")
                tasksShouldNotBeTriggered(":$moduleName:validateNetworkContractsByRemote")
                tasksShouldNotBeTriggered(":$moduleName:collectApiSchemes")
            }
    }

    @ParameterizedTest
    @ArgumentsSource(ValidationByCodegenArgumentSource::class)
    fun `when run validation task and codegenToml is omitted -  then throw validation error`(
        validationByCodegen: Boolean,
        @TempDir projectDir: File
    ) {
        val moduleName = "app"
        generateProjectWithGeneratedFiles(
            projectDir,
            generatedFiles = emptyList(),
            moduleName = moduleName,
            failFast = true,
            validationByCodegen = validationByCodegen,
        )
        projectDir.dir(moduleName).file("codegen.toml").delete()

        runTask("$moduleName:${ValidateNetworkContractsTask.NAME}", projectDir, failed = true)
            .assertThat()
            .buildFailed()
            .outputContains("codegen.toml file is omitted in the `:$moduleName` module")
            .apply {
                tasksShouldBeTriggered(":$moduleName:validateNetworkContractsFiles")
                tasksShouldNotBeTriggered(":$moduleName:validateNetworkContractsByRemote")
                tasksShouldNotBeTriggered(":$moduleName:collectApiSchemes")
            }
    }

    private fun generateProjectWithGeneratedFiles(
        projectDir: File,
        generatedFiles: List<File>,
        moduleName: String = "app",
        schemes: List<SchemaEntry> = listOf(
            SchemaEntry("test/path.yaml", "content")
        ),
        failFast: Boolean = false,
        validationByCodegen: Boolean = true,
    ): List<File> {
        val packageName = "com.avito.android"

        val validateTaskExtraConfiguration = configureTestValidationTask()

        NetworkCodegenProjectGenerator.generate(
            projectDir,
            modules = listOf(
                defaultModule(
                    name = moduleName,
                    generatedClassesPackage = packageName,
                    failFast = failFast,
                    buildExtra = validateTaskExtraConfiguration,
                    validationByCodegen = validationByCodegen
                )
            )
        )
        NetworkCodegenProjectGenerator.generateSchemes(projectDir = File(projectDir, moduleName), schemes = schemes)
        return NetworkCodegenProjectGenerator.generateCodegenFiles(moduleName, projectDir, packageName, generatedFiles)
    }

    private fun runTask(
        name: String,
        tempDir: File,
        failed: Boolean = false,
        dryRun: Boolean = false,
    ): TestResult {
        return gradlew(
            tempDir,
            ":$name",
            "-Pavito.clickstream.serviceUrl=stub",
            expectFailure = failed,
            dryRun = dryRun,
            configurationCache = true,
            useTestFixturesClasspath = true
        )
    }

    private fun configureTestValidationTask(): String {
        return """
            tasks.named(
                "${CodegenTask.NAME}Validate", 
                DefaultTask::class.java
            ).configure {
                isEnabled = false
            }
           
        """.trimIndent()
    }
}

private class ValidationByCodegenArgumentSource : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        return Stream.of(
            Arguments.of(true),
            Arguments.of(false),
        )
    }
}
