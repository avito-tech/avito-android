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
            .outputContains("must include all implementations/fakes for public dependencies of logical modules.")
            .outputDoesNotContain("Possible implementations: []")
    }

    private fun runCheck(projectDir: File, expectFailure: Boolean = false) = gradlew(
        projectDir,
        ValidatePublicDependenciesImplementedTask.NAME,
        "-Dorg.gradle.caching=true",
        expectFailure = expectFailure,
        useTestFixturesClasspath = true,
    )
}
