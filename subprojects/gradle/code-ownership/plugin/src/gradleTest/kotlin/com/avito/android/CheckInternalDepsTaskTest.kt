package com.avito.android

import com.avito.android.check.deps.CheckInternalDepsTask
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class CheckInternalDepsTaskTest {

    @Test
    internal fun `check internal dependencies - no readme - build failed`(@TempDir projectDir: File) {
        generateProject(projectDir, hasReadme = false)
        runCheck(projectDir, isEnabled = true, expectFailure = true)
            .assertThat()
            .buildFailed()
    }

    @Test
    internal fun `check internal dependencies - has readme - build successful`(@TempDir projectDir: File) {
        generateProject(projectDir, hasReadme = true)
        runCheck(projectDir, isEnabled = true, expectFailure = false)
            .assertThat()
            .buildSuccessful()
    }

    @Test
    internal fun `check internal dependencies - disabled and no readme - build failed`(@TempDir projectDir: File) {
        generateProject(projectDir, hasReadme = false)
        runCheck(projectDir, isEnabled = false, expectFailure = true)
            .assertThat()
            .buildFailed()
    }

    @Test
    internal fun `check internal dependencies - disabled and has readme - build failed`(@TempDir projectDir: File) {
        generateProject(projectDir, hasReadme = true)
        runCheck(projectDir, isEnabled = false, expectFailure = true)
            .assertThat()
            .buildFailed()
    }

    private fun generateProject(projectDir: File, hasReadme: Boolean) {
        TestProjectGenerator(
            name = "rootapp",
            plugins = plugins {
                id("com.avito.android.code-ownership")
            },
            useKts = true,
            modules = listOf(
                AndroidAppModule(
                    "app",
                    plugins = plugins {
                        id("com.avito.android.code-ownership")
                    },
                    useKts = true,
                    mutator = {
                        if (hasReadme) {
                            parentFile.file(name = "README.md", content = "logical module description")
                        }
                    }
                ),
            ),
        ).generateIn(projectDir)
    }

    private fun runCheck(
        projectDir: File,
        isEnabled: Boolean,
        expectFailure: Boolean,
    ) = gradlew(
        projectDir,
        CheckInternalDepsTask.NAME,
        "-Dorg.gradle.caching=true",
        "-Pavito.ownership.internal-deps-check.enable=$isEnabled",
        expectFailure = expectFailure
    )
}
