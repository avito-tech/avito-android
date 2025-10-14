package com.avito.android.contracts.platform.validation

import com.avito.android.contracts.platform.ContractsTaskNamesBuilder
import com.avito.android.contracts.platform.NetworkCodegenProjectGenerator
import com.avito.android.contracts.platform.defaultModule
import com.avito.android.contracts.platform.scheme.imports.data.models.SchemaEntry
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ValidationTasksTest {

    @Test
    fun `when validation task is invoked with validationByCodegen enabled - then codegen validation task is triggered`(
        @TempDir projectDir: File
    ) {
        val moduleName = "feature"
        val variants = listOf(NetworkCodegenProjectGenerator.Variant(name = "test", validationByCodegen = true))
        generateProjectWithGeneratedFiles(
            projectDir = projectDir,
            moduleName = moduleName,
            variants = variants
        )

        runTask(
            taskName = ContractsTaskNamesBuilder.validationTask("all"),
            projectDir = projectDir,
            dryRun = true
        ).assertThat()
            .buildSuccessful()
            .tasksShouldBeTriggered(
                ":$moduleName:${ContractsTaskNamesBuilder.validationTask("test", "all")}",
                ":${ContractsTaskNamesBuilder.validationTask("all")}"
            )
            .inOrder()
    }

    @Test
    fun `when validation task is invoked with validationByCodegen disabled - then remote validation task is triggered`(
        @TempDir projectDir: File
    ) {
        val moduleName = "feature"
        val variants = listOf(NetworkCodegenProjectGenerator.Variant(name = "test", validationByCodegen = false))
        generateProjectWithGeneratedFiles(
            projectDir = projectDir,
            moduleName = moduleName,
            variants = variants
        )

        runTask(
            taskName = ContractsTaskNamesBuilder.validationTask("all"),
            projectDir = projectDir,
            dryRun = true
        ).assertThat()
            .buildSuccessful()
            .tasksShouldBeTriggered(
                ":$moduleName:${ContractsTaskNamesBuilder.validationTask("test", "all")}",
                ":${ContractsTaskNamesBuilder.validationTask("all")}"
            )
            .inOrder()
    }

    private fun generateProjectWithGeneratedFiles(
        projectDir: File,
        moduleName: String = "feature",
        variants: List<NetworkCodegenProjectGenerator.Variant> = listOf(
            NetworkCodegenProjectGenerator.Variant()),
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
        dryRun: Boolean = false,
        expectFailure: Boolean = false
    ): TestResult {
        val args = mutableListOf<String>()
        args.add(":$taskName")
        args.add("-Pavito.clickstream.serviceUrl=stub")

        return gradlew(
            projectDir = projectDir,
            dryRun = dryRun,
            expectFailure = expectFailure,
            useTestFixturesClasspath = true,
            args = args.toTypedArray(),
        )
    }
}
