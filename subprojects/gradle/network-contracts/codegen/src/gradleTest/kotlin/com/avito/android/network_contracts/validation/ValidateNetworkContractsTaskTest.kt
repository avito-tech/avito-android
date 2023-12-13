package com.avito.android.network_contracts.validation

import com.avito.android.network_contracts.NetworkCodegenProjectGenerator
import com.avito.android.network_contracts.codegen.CodegenTask
import com.avito.android.network_contracts.defaultModule
import com.avito.android.network_contracts.test.ChangeReferencesFilesTask
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import org.intellij.lang.annotations.Language
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
                ":$projectName:${ValidateNetworkContractsTask.NAME}",
                ":$projectName:${CodegenTask.NAME}"
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
    fun `when run validation task and files is corrupted - then throw validation error - then add it to git`(
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
            .outputContains("Validation of the generated files failed")
            .outputContains("Found corrupted files")
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
            name, "-PcorruptFile=$corruptFile",
            expectFailure = failed,
            dryRun = dryRun,
            configurationCache = true,
            useTestFixturesClasspath = true
        )
    }

    @Language("kotlin")
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
                dependsOn("prepareCodegenSnapshot")
            }
            
            tasks.named(
                "${ValidateNetworkContractsTask.NAME}", 
                ${ValidateNetworkContractsTask::class.qualifiedName}::class.java
            ).configure {
                referenceFilesDirectory.set(changeReferencesFilesTask.flatMap { it.outputDirectory })
            }
        """.trimIndent()
    }
}
