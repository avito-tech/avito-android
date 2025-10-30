@file:Suppress("MaxLineLength")

package com.avito.android.contracts.scheme.validation

import com.avito.android.contracts.network.NetworkCodegenProjectGenerator
import com.avito.android.contracts.network.defaultModule
import com.avito.android.contracts.platform.ContractsTaskNamesBuilder
import com.avito.android.contracts.platform.scheme.imports.data.models.SchemaEntry
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.dir
import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
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
    fun `when root validation task is invoked - validationByCodegen is false - then invoke modules validation report task with codegen`(
        @TempDir projectDir: File
    ) {
        val projectName = "feature"
        generateProjectWithGeneratedFiles(
            projectDir,
            emptyList(),
            moduleName = projectName,
            validationByCodegen = false
        )
        val assert = runTask(ContractsTaskNamesBuilder.validationTask("all"), projectDir, dryRun = true)
            .assertThat()

        assert.apply {
            tasksShouldBeTriggered(
                ":$projectName:${ContractsTaskNamesBuilder.validationTask("network", "local")}",
                ":$projectName:${ContractsTaskNamesBuilder.collectSchemesTask("network")}",
                ":$projectName:${ContractsTaskNamesBuilder.validationTask("network", "remote")}",
                ":$projectName:${ContractsTaskNamesBuilder.validationTask("network", "all")}",
                ":${ContractsTaskNamesBuilder.validationTask("all")}",
            )
                .inOrder()

            tasksShouldNotBeTriggered(
                ":$projectName:${ContractsTaskNamesBuilder.codegenTask()}"
            )
        }
    }

    @Test
    fun `when validation task called with codegen - then invoke tasks in right order`(
        @TempDir projectDir: File
    ) {
        val projectName = "feature"
        generateProjectWithGeneratedFiles(
            projectDir,
            emptyList(),
            moduleName = projectName,
            validationByCodegen = false
        )
        val assert = runTasks(
            listOf(ContractsTaskNamesBuilder.codegenTask(), ContractsTaskNamesBuilder.validationTask("all")),
            projectDir,
            dryRun = true
        )
            .assertThat()

        assert
            .tasksShouldBeTriggered(
                ":$projectName:${ContractsTaskNamesBuilder.validationTask("network", "local")}",
                ":$projectName:${ContractsTaskNamesBuilder.collectSchemesTask("network")}",
                ":$projectName:${ContractsTaskNamesBuilder.validationTask("network", "remote")}",
                ":$projectName:${ContractsTaskNamesBuilder.codegenTask()}",
            )
            .inOrder()
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
        runTask(ContractsTaskNamesBuilder.validationTask("all"), projectDir, failed = true)
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
            validationByCodegen = validationByCodegen
        )
        runTask("$moduleName:${ContractsTaskNamesBuilder.validationTask("network", "all")}", projectDir, failed = true)
            .assertThat()
            .buildFailed()
            .outputContains("Module `:$moduleName` applies plugin, but does not contain any network contracts schemes.")
            .apply {
                tasksShouldBeTriggered(":$moduleName:${ContractsTaskNamesBuilder.validationTask("network", "local")}")
                tasksShouldNotBeTriggered(
                    ":$moduleName:${
                        ContractsTaskNamesBuilder.validationTask(
                            "network",
                            "remote"
                        )
                    }"
                )
                tasksShouldNotBeTriggered(":$moduleName:${ContractsTaskNamesBuilder.collectSchemesTask("network")}")
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
            validationByCodegen = validationByCodegen,
        )
        projectDir.dir(moduleName).file("codegen.toml").delete()

        runTask("$moduleName:${ContractsTaskNamesBuilder.validationTask("network", "all")}", projectDir, failed = true)
            .assertThat()
            .buildFailed()
            .outputContains("codegen.toml file is omitted in the `:$moduleName` module")
            .apply {
                tasksShouldBeTriggered(":$moduleName:${ContractsTaskNamesBuilder.validationTask("network", "local")}")
                tasksShouldNotBeTriggered(
                    ":$moduleName:${
                        ContractsTaskNamesBuilder.validationTask(
                            "network",
                            "remote"
                        )
                    }"
                )
                tasksShouldNotBeTriggered(":$moduleName:${ContractsTaskNamesBuilder.collectSchemesTask("network")}")
            }
    }

    private fun generateProjectWithGeneratedFiles(
        projectDir: File,
        generatedFiles: List<File>,
        moduleName: String = "app",
        schemes: List<SchemaEntry> = listOf(
            SchemaEntry("test/path.yaml", "content")
        ),
        validationByCodegen: Boolean = true,
    ): List<File> {
        val packageName = "com.avito.android"

        NetworkCodegenProjectGenerator.generate(
            projectDir,
            modules = listOf(
                defaultModule(
                    name = moduleName,
                    generatedClassesPackage = packageName,
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
        return runTasks(listOf(name), tempDir, failed, dryRun)
    }

    private fun runTasks(
        names: List<String>,
        tempDir: File,
        failed: Boolean = false,
        dryRun: Boolean = false,
    ): TestResult {
        return gradlew(
            tempDir,
            *names.toTypedArray(),
            "-Pavito.clickstream.serviceUrl=stub",
            expectFailure = failed,
            dryRun = dryRun,
            configurationCache = true,
            useTestFixturesClasspath = true
        )
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
