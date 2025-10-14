package com.avito.android.contracts.platform.fixation

import com.avito.android.contracts.platform.ContractsTaskNamesBuilder
import com.avito.android.contracts.platform.NetworkCodegenProjectGenerator
import com.avito.android.contracts.platform.defaultModule
import com.avito.android.contracts.platform.scheme.imports.data.models.SchemaEntry
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class FixationTasksTest {

    @Test
    fun `when fixation task is invoked - then required tasks are triggered in correct order`(
        @TempDir projectDir: File
    ) {
        val moduleName = "feature"
        generateProjectWithGeneratedFiles(
            projectDir = projectDir,
            moduleName = moduleName,
        )

        runTask(
            taskName = ContractsTaskNamesBuilder.updateSchemesTask("all"),
            projectDir = projectDir,
            author = "test-author",
            dryRun = true
        ).assertThat()
            .buildSuccessful()
            .tasksShouldBeTriggered(
                ":$moduleName:${ContractsTaskNamesBuilder.collectSchemesTask("test")}",
                ":$moduleName:${ContractsTaskNamesBuilder.validationTask("test", "all")}",
                ":${ContractsTaskNamesBuilder.updateSchemesTask("test")}",
                ":${ContractsTaskNamesBuilder.updateSchemesTask("all")}"
            )
            .inOrder()
    }

    @Test
    fun `when both validation and fixation tasks are invoked - then all tasks are triggered correctly`(
        @TempDir projectDir: File
    ) {
        val moduleName = "feature"
        generateProjectWithGeneratedFiles(
            projectDir = projectDir,
            moduleName = moduleName,
            variants = listOf(
                NetworkCodegenProjectGenerator.Variant(name = "test1"),
                NetworkCodegenProjectGenerator.Variant(name = "test2"),

                )
        )

        runTask(
            taskName = ContractsTaskNamesBuilder.updateSchemesTask("all"),
            projectDir = projectDir,
            author = "test-author",
            dryRun = true
        ).assertThat()
            .buildSuccessful()
            .tasksShouldBeTriggered(
                ":$moduleName:${ContractsTaskNamesBuilder.collectSchemesTask("test1")}",
                ":$moduleName:${ContractsTaskNamesBuilder.validationTask("test1", "all")}",
                ":${ContractsTaskNamesBuilder.updateSchemesTask("test1")}",
                ":$moduleName:${ContractsTaskNamesBuilder.collectSchemesTask("test2")}",
                ":$moduleName:${ContractsTaskNamesBuilder.validationTask("test2", "all")}",
                ":${ContractsTaskNamesBuilder.updateSchemesTask("test2")}",
                ":${ContractsTaskNamesBuilder.updateSchemesTask("all")}"
            )
    }

    @Test
    fun `when fixation or validation is disabled - then tasks is not invoked`(
        @TempDir projectDir: File
    ) {
        val moduleName = "feature"
        generateProjectWithGeneratedFiles(
            projectDir = projectDir,
            moduleName = moduleName,
            variants = listOf(
                NetworkCodegenProjectGenerator.Variant(name = "test1", validationEnabled = false),
                NetworkCodegenProjectGenerator.Variant(name = "test2", fixationEnabled = false),
            )
        )

        runTask(
            taskName = ContractsTaskNamesBuilder.updateSchemesTask("all"),
            projectDir = projectDir,
            author = "test-author",
            dryRun = true
        ).assertThat()
            .buildSuccessful()
            .apply {
                tasksShouldBeTriggered(
                    ":$moduleName:${ContractsTaskNamesBuilder.collectSchemesTask("test1")}",
                    ":${ContractsTaskNamesBuilder.updateSchemesTask("test1")}",
                    ":${ContractsTaskNamesBuilder.updateSchemesTask("all")}"
                )
                tasksShouldNotBeTriggered(
                    ":$moduleName:${ContractsTaskNamesBuilder.validationTask("test1", "all")}",
                    ":$moduleName:${ContractsTaskNamesBuilder.collectSchemesTask("test2")}",
                    ":${ContractsTaskNamesBuilder.updateSchemesTask("test2")}",
                )
            }
    }

    private fun generateProjectWithGeneratedFiles(
        projectDir: File,
        moduleName: String = "feature",
        variants: List<NetworkCodegenProjectGenerator.Variant> = listOf(NetworkCodegenProjectGenerator.Variant()),
        schemes: List<SchemaEntry> = listOf(
            SchemaEntry("test/path.yaml", "content")
        )
    ) {
        NetworkCodegenProjectGenerator.generate(
            projectDir = projectDir,
            variants = variants,
            modules = listOf(
                defaultModule(
                    name = moduleName,
                    variants = variants,
                )
            )
        )

        NetworkCodegenProjectGenerator.generateSchemes(
            projectDir = File(projectDir, moduleName),
            schemes = schemes
        )
    }

    private fun runTask(
        taskName: String,
        projectDir: File,
        author: String = "",
        dryRun: Boolean = false,
        expectFailure: Boolean = false
    ): TestResult {
        val args = mutableListOf<String>()
        args.add(":$taskName")

        if (author.isNotEmpty()) {
            args.add("-Pavito.networkContracts.fixation.author=$author")
        }

        args.add("-Pavito.clickstream.serviceUrl=stub")

        return gradlew(
            projectDir = projectDir,
            dryRun = dryRun,
            expectFailure = expectFailure,
            useTestFixturesClasspath = true,
            args = args.toTypedArray()
        )
    }
}
