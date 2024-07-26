package com.avito.android.module_type.validation

import com.avito.android.module_type.FunctionalType
import com.avito.android.module_type.validation.configurations.missings.implementations.MissingImplementationDependencyTask
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.plugin.plugins
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
    internal fun `validate dependencies - debug connected - success`(@TempDir projectDir: File) {
        DependenciesValidationProjectGenerator.generateProject(
            projectDir,
            connectedFunctionalType = FunctionalType.Debug
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
    internal fun `connect validation plugin before module types plugin - success`(@TempDir projectDir: File) {
        DependenciesValidationProjectGenerator.generateProject(
            projectDir,
            connectedFunctionalType = FunctionalType.Fake,
            rootPlugins = plugins {
                id("com.avito.android.module-types-validator")
                id("com.avito.android.module-types")
            }
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
            .outputContains("Please, add impl/fake/debug dependencies to the build file")
            .outputContains(":lib-b:demo -> :lib-b:impl -> :lib-b:public -> :lib-a:public")
            .outputContains(":lib-c:demo -> :lib-c:impl -> :lib-b:public -> :lib-a:public")
            .outputContains("implementation(projects.libA.impl)")
            .outputContains("implementation(projects.libA.fake)")
            .outputContains("implementation(projects.libA.debug)")
    }

    @Test
    internal fun `apply validation plugin to library module - failure`(@TempDir projectDir: File) {
        DependenciesValidationProjectGenerator.generateProject(
            projectDir,
            implModulePlugins = plugins {
                id("com.avito.android.module-types")
                id("com.avito.android.module-types-validator")
            }
        )
        runCheck(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains("This validation check should be performed only on demo or application modules")
    }

    private fun runCheck(projectDir: File, expectFailure: Boolean = false) = gradlew(
        projectDir,
        MissingImplementationDependencyTask.NAME,
        expectFailure = expectFailure,
        useTestFixturesClasspath = true,
    )
}
