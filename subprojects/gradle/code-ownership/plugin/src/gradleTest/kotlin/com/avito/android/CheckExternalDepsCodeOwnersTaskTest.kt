package com.avito.android

import com.avito.android.check.CheckExternalDepsCodeOwners
import com.avito.android.utils.FAKE_OWNERSHIP_EXTENSION
import com.avito.android.utils.FAKE_OWNERS_PROVIDER_EXTENSION
import com.avito.android.utils.LIBS_OWNERS_TOML_CONTENT
import com.avito.android.utils.LIBS_VERSIONS_TOML_CONTENT
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.plugin.plugins
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class CheckExternalDepsCodeOwnersTaskTest {

    @Test
    internal fun `check dependencies - no expectedOwnersProvider - build failed`(@TempDir projectDir: File) {
        generateProject(projectDir, hasExpectedOwnersProvider = false)
        runCheck(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
    }

    @Test
    internal fun `check dependencies - no ownerSerializer - build failed`(@TempDir projectDir: File) {
        generateProject(projectDir, hasOwnersSerializer = false)
        runCheck(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
    }

    @Test
    internal fun `check dependencies - no libs_versions_toml - build failed`(@TempDir projectDir: File) {
        generateProject(projectDir, hasLibsVersions = false)
        runCheck(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
    }

    @Test
    internal fun `check dependencies - no libs_owners_toml - build failed`(@TempDir projectDir: File) {
        generateProject(projectDir, hasLibsOwners = false)
        runCheck(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
    }

    @Test
    internal fun `check dependencies - everything present - build success`(@TempDir projectDir: File) {
        generateProject(projectDir)
        runCheck(projectDir, expectFailure = false)
            .assertThat()
            .buildSuccessful()
    }

    @Test
    internal fun `check dependencies twice - remove output - build cache used`(@TempDir projectDir: File) {
        generateProject(projectDir)

        runCheck(projectDir)
            .assertThat()
            .buildSuccessful()

        val output = File(projectDir, "build/reports/check_external_dependencies.report")
        output.delete()

        runCheck(projectDir)
            .assertThat()
            .buildSuccessful()
            .taskWithOutcome(":${CheckExternalDepsCodeOwners.NAME}", TaskOutcome.FROM_CACHE)
    }

    @Test
    internal fun `check dependencies twice - result is up to date`(@TempDir projectDir: File) {
        generateProject(projectDir)

        runCheck(projectDir)
            .assertThat()
            .buildSuccessful()

        runCheck(projectDir)
            .assertThat()
            .buildSuccessful()
            .taskWithOutcome(":${CheckExternalDepsCodeOwners.NAME}", TaskOutcome.UP_TO_DATE)
    }

    private fun generateProject(
        projectDir: File,
        hasExpectedOwnersProvider: Boolean = true,
        hasOwnersSerializer: Boolean = true,
        hasLibsVersions: Boolean = true,
        hasLibsOwners: Boolean = true,
    ) {

        if (hasLibsVersions) projectDir.file("gradle/libs.versions.toml", LIBS_VERSIONS_TOML_CONTENT)
        if (hasLibsOwners) projectDir.file("gradle/libs.owners.toml", LIBS_OWNERS_TOML_CONTENT)
        TestProjectGenerator(
            name = "rootapp",
            plugins = plugins {
                id("com.avito.android.code-ownership")
            },
            useKts = true,
            buildGradleExtra = """
                ${if (hasOwnersSerializer) FAKE_OWNERSHIP_EXTENSION else ""}
                ${if (hasExpectedOwnersProvider) FAKE_OWNERS_PROVIDER_EXTENSION else ""}
            """.trimIndent()
        ).generateIn(projectDir)
    }

    private fun runCheck(projectDir: File, expectFailure: Boolean = false) = gradlew(
        projectDir,
        CheckExternalDepsCodeOwners.NAME,
        "-Dorg.gradle.caching=true",
        expectFailure = expectFailure
    )
}
