package com.avito.android.network_contracts.codegen

import com.avito.android.network_contracts.NetworkCodegenProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.google.common.truth.Truth
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class CodegenTest {

    @Test
    fun `codegen task - applies subtasks in the correct order`(@TempDir projectDir: File) {
        NetworkCodegenProjectGenerator.generate(projectDir)

        runTask(projectDir, CodegenTask.NAME, dryRun = true)
            .assertThat()
            .buildSuccessful()
            .tasksShouldBeTriggered(":${MakeFilesExecutableTask.NAME}", ":${SetupTmpMtlsFilesTask.NAME}")
            .inOrder()
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

    private fun runTask(tempDir: File, name: String, dryRun: Boolean = false): TestResult {
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
