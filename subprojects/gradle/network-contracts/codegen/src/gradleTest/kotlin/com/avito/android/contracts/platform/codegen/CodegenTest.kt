package com.avito.android.contracts.platform.codegen

import com.avito.android.contracts.platform.ContractsTaskNamesBuilder
import com.avito.android.contracts.platform.NetworkCodegenProjectGenerator
import com.avito.android.contracts.platform.defaultAndroidModule
import com.avito.android.contracts.platform.defaultModule
import com.avito.android.contracts.platform.scheme.codegen.SetupTmpMtlsFilesTask
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.google.common.truth.Truth
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class CodegenTest {

    @Test
    fun `compile task - applies subtasks in the correct order`(@TempDir projectDir: File) {
        val variants = listOf(NetworkCodegenProjectGenerator.Variant())
        val module = defaultModule(variants = variants)
        NetworkCodegenProjectGenerator.generate(projectDir, variants, modules = listOf(module))
        runTask(projectDir, "compileKotlin", dryRun = true)
            .assertThat()
            .buildSuccessful()
            .apply {
                tasksShouldBeTriggered(
                    ":${module.name}:codegen",
                )
                tasksShouldNotBeTriggered(
                    ":${SetupTmpMtlsFilesTask.NAME}"
                )
            }
    }

    @Test
    fun `compile android task - applies subtasks in the correct order`(@TempDir projectDir: File) {
        val variants = listOf(NetworkCodegenProjectGenerator.Variant())
        val module = defaultAndroidModule(variants = variants)
        NetworkCodegenProjectGenerator.generate(projectDir, variants, modules = listOf(module))
        runTask(projectDir, "compileReleaseKotlin", dryRun = true)
            .assertThat()
            .buildSuccessful()
            .apply {
                tasksShouldBeTriggered(
                    ":${module.name}:codegenRelease",
                )
                tasksShouldNotBeTriggered(
                    ":${SetupTmpMtlsFilesTask.NAME}"
                )
            }
    }

    @Test
    fun `codegen task - applies subtasks in the correct order`(@TempDir projectDir: File) {
        val variants = listOf(NetworkCodegenProjectGenerator.Variant())
        val module = defaultModule(variants = variants)
        NetworkCodegenProjectGenerator.generate(projectDir, variants, modules = listOf(module))

        val codegenTaskName = ContractsTaskNamesBuilder.codegenTask()
        runTask(projectDir, codegenTaskName, dryRun = true)
            .assertThat()
            .buildSuccessful()
            .apply {
                tasksShouldBeTriggered(":${module.name}:$codegenTaskName").inOrder()
                tasksShouldNotBeTriggered(":${SetupTmpMtlsFilesTask.NAME}")
            }
    }

    @Test
    fun `setup mTLS task runs - the temporary files are created`(@TempDir projectDir: File) {
        NetworkCodegenProjectGenerator.generate(projectDir)
        val projectBuildDir = "${projectDir.path}/build/${SetupTmpMtlsFilesTask.NAME}"
        val tmpFiles = listOf("tmp_mtls_crt.crt", "tmp_mtls_key.key")

        runTask(projectDir, SetupTmpMtlsFilesTask.NAME)
            .assertThat()
            .buildSuccessful()

        tmpFiles.forEach {
            Truth.assertThat(File(projectBuildDir, it).exists())
                .isTrue()
        }
    }

    private fun runTask(
        tempDir: File,
        name: String,
        dryRun: Boolean = false,
    ): TestResult {
        return gradlew(
            tempDir,
            name,
            "-Pavito.ownership.mtlsCrt=\"\${OWNERSHIP_MTLS_CRT_CI}\"",
            "-Pavito.ownership.mtlsKey=\"\${OWNERSHIP_MTLS_CRT_KEY_CI}\"\n",
            useTestFixturesClasspath = true,
            dryRun = dryRun
        )
    }
}
