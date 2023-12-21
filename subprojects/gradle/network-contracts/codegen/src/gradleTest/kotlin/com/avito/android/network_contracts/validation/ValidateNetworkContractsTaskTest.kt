package com.avito.android.network_contracts.validation

import com.avito.android.network_contracts.NetworkCodegenProjectGenerator
import com.avito.android.network_contracts.codegen.CodegenTask
import com.avito.android.network_contracts.defaultModule
import com.avito.android.network_contracts.scheme.imports.data.models.SchemaEntry
import com.avito.android.network_contracts.snapshot.PrepareGeneratedCodeSnapshotTask
import com.avito.android.network_contracts.test.ChangeReferencesFilesTask
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
        NetworkCodegenProjectGenerator.generate(projectDir, modules = defaultModule(name = projectName))
        runTask(ValidateNetworkContractsRootTask.NAME, projectDir, dryRun = true)
            .assertThat()
            .tasksShouldBeTriggered(
                ":$projectName:${PrepareGeneratedCodeSnapshotTask.NAME}",
                ":$projectName:${CodegenTask.NAME}",
                ":$projectName:${ValidateNetworkContractsGeneratedFilesTask.NAME}",
                ":$projectName:${ValidateNetworkContractsSchemesTask.NAME}",
                ":${ValidateNetworkContractsRootTask.NAME}",
            )
    }

    @Test
    fun `when run validation task and files is not corrupted - then return success result`(
        @TempDir projectDir: File
    ) {
        val codegenFiles = listOf(File("generated.kt"))
        generateProjectWithGeneratedFiles(projectDir, codegenFiles)
        runTask(ValidateNetworkContractsRootTask.NAME, projectDir)
            .assertThat()
            .buildSuccessful()
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

    @Test
    fun `when run validation task and files is corrupted - then throw validation error - then fix them`(
        @TempDir projectDir: File
    ) {
        // given: generated files
        val codegenFiles = listOf(
            File("generated.kt"),
            File("scheme/generated.kt")
        )

        val generatedFiles = generateProjectWithGeneratedFiles(projectDir, codegenFiles)
        generatedFiles.forEach { it.writeText("") }

        // when: corrupt files and run validation task
        val taskAssert = runTask(ValidateNetworkContractsRootTask.NAME, projectDir, failed = true, corruptFile = true)

        // then: report fail result
        taskAssert.assertThat()
            .buildFailed()
            .outputContains("Validation of the network contracts plugin failed")
            .apply {
                codegenFiles.forEach { outputContains(it.path) }
            }

        // when: fix files as after codegen
        generatedFiles.forEach { it.writeText("") }

        // then: success result
        runTask(ValidateNetworkContractsRootTask.NAME, projectDir, corruptFile = false)
            .assertThat()
            .buildSuccessful()
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

        val validateTaskExtraConfiguration = configureTestValidationTask(packageName)

        NetworkCodegenProjectGenerator.generate(
            projectDir,
            modules = defaultModule(
                name = moduleName,
                generatedClassesPackage = packageName,
                buildExtra = validateTaskExtraConfiguration
            )
        )
        NetworkCodegenProjectGenerator.generateSchemes(projectDir, moduleName = moduleName, schemes = schemes)
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

    private fun configureTestValidationTask(
        packageName: String,
    ): String {
        val generatedDirGradlePath = """
             project.layout.projectDirectory.dir("src/main/kotlin/${packageName.replace(".", "/")}/generated")
        """.trimIndent()

        return """
            val changeReferencesFilesTask = tasks.register(
                "${ChangeReferencesFilesTask.NAME}",
                ${ChangeReferencesFilesTask::class.qualifiedName}::class.java
            ) {
                referencesFiles.setFrom($generatedDirGradlePath)
                outputDirectory.set($generatedDirGradlePath)
                isEnabled = project.property("corruptFile") == "true"
                dependsOn("${CodegenTask.NAME}")
            }
            
            tasks.named(
                "${CodegenTask.NAME}", 
                DefaultTask::class.java
            ).configure {
                isEnabled = false
            }
            
            tasks.named(
                "${ValidateNetworkContractsGeneratedFilesTask.NAME}", 
                ${ValidateNetworkContractsGeneratedFilesTask::class.qualifiedName}::class.java
            ).configure {
                referenceFilesDirectory.set(changeReferencesFilesTask.flatMap { it.outputDirectory })
            }
        """.trimIndent()
    }
}
