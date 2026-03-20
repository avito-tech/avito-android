package com.avito.android.module_type.validation

import com.avito.android.module_type.FunctionalType
import com.avito.android.module_type.validation.configurations.forbidden.demo_dependencies.ForbiddenDemoDependenciesTask
import com.avito.android.module_type.validation.configurations.missings.implementations.MissingImplementationDependencyTask
import com.avito.test.gradle.gradlew
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ConfigurationCacheCompatibilityTest {

    @Test
    fun `configuration with applied plugin - ok`(@TempDir projectDir: File) {
        DependenciesValidationProjectGenerator.generateProject(
            projectDir,
            connectedFunctionalType = FunctionalType.Impl
        )

        runCheck(projectDir)
            .assertThat()
            .buildSuccessful()

        runCheck(projectDir)
            .assertThat()
            .buildSuccessful()
            .configurationCachedReused()
    }

    @Test
    fun `configuration cache for forbidden demo dependencies - ok`(@TempDir projectDir: File) {
        DependenciesValidationProjectGenerator.generateProject(
            projectDir,
            connectedFunctionalType = FunctionalType.Fake,
            forbiddenDemoDependencyConnected = true,
            allowForbiddenDemoDependency = true,
        )

        runForbiddenDemoDependenciesCheck(projectDir)
            .assertThat()
            .buildSuccessful()

        runForbiddenDemoDependenciesCheck(projectDir)
            .assertThat()
            .buildSuccessful()
            .configurationCachedReused()
    }

    private fun runCheck(projectDir: File) = gradlew(
        projectDir,
        MissingImplementationDependencyTask.NAME,
        useTestFixturesClasspath = true,
        configurationCache = true
    )

    private fun runForbiddenDemoDependenciesCheck(projectDir: File) = gradlew(
        projectDir,
        ":lib-c:demo:${ForbiddenDemoDependenciesTask.NAME}",
        useTestFixturesClasspath = true,
        configurationCache = true
    )
}
