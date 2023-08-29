package com.avito.android.module_type.validation

import com.avito.android.module_type.FunctionalType
import com.avito.android.module_type.validation.publicimpl.ValidatePublicDependenciesImplementedTask
import com.avito.test.gradle.gradlew
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ValidateProjectDependenciesTest {

    @Test
    internal fun `validate dependencies - impl connected - success`(@TempDir projectDir: File) {
        DependenciesValidationProjectGenerator.generateProject(
            projectDir,
            connectedFunctionalType = FunctionalType.Impl
        )

        runCheck(projectDir, expectFailure = false)
            .assertThat()
            .buildSuccessful()
    }

    @Test
    internal fun `validate dependencies - fake connected - success`(@TempDir projectDir: File) {
        DependenciesValidationProjectGenerator.generateProject(
            projectDir,
            connectedFunctionalType = FunctionalType.Fake
        )
        runCheck(projectDir, expectFailure = false)
            .assertThat()
            .buildSuccessful()
    }

    @Test
    internal fun `validate dependencies - impl or fake does not connected - failure`(@TempDir projectDir: File) {
        DependenciesValidationProjectGenerator.generateProject(projectDir)
        runCheck(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains("Please, add impl/fake dependencies to the build file")
            .outputContains("implementation(projects.libA.impl)")
            .outputContains("implementation(projects.libA.fake)")
    }

    @Test
    internal fun `apply validation plugin to library module - failure`(@TempDir projectDir: File) {
        DependenciesValidationProjectGenerator.generateProject(projectDir, applyPluginToImpl = true)
        runCheck(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains("This validation check should be performed only on demo or application modules")
    }

    private fun runCheck(projectDir: File, expectFailure: Boolean = false) = gradlew(
        projectDir,
        ValidatePublicDependenciesImplementedTask.NAME,
        "-Dorg.gradle.caching=true",
        expectFailure = expectFailure,
        useTestFixturesClasspath = true,
    )
}
