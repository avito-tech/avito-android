package com.avito.android.module_type.validation

import com.avito.test.gradle.gradlew
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ForbiddenDemoDependenciesTest {

    @Test
    internal fun `demo app without forbidden dependency - success`(@TempDir projectDir: File) {
        DependenciesValidationProjectGenerator.generateProject(
            projectDir,
        )

        runCheck(projectDir, expectFailure = false)
            .assertThat()
            .buildSuccessful()
    }

    @Test
    internal fun `demo app with forbidden dependency - failure`(@TempDir projectDir: File) {
        DependenciesValidationProjectGenerator.generateProject(
            projectDir,
            forbiddenDemoDependencyConnected = true,
        )

        runCheck(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains(":lib-c:demo depends on forbidden modules.")
            .outputContains("See docs for details: https://links.k.avito.ru/android-forbidden-demo-dependencies")
            .outputContains("Forbidden modules:")
            .outputContains(":heavy-module")
            .outputContains("Dependency for :heavy-module appears from:")
            .outputContains(":lib-c:demo -> :heavy-module")
    }

    @Test
    internal fun `demo app with allowed forbidden dependency - success`(@TempDir projectDir: File) {
        DependenciesValidationProjectGenerator.generateProject(
            projectDir,
            forbiddenDemoDependencyConnected = true,
            allowForbiddenDemoDependency = true,
        )

        runCheck(projectDir, expectFailure = false)
            .assertThat()
            .buildSuccessful()
    }

    @Test
    internal fun `demo app with unused allow - failure`(@TempDir projectDir: File) {
        DependenciesValidationProjectGenerator.generateProject(
            projectDir,
            allowForbiddenDemoDependency = true,
        )

        runCheck(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains("Some allow() entries in lib-c/demo/build.gradle are excessive. Remove them:")
            .outputContains("""allow(":heavy-module")""")
    }

    @Test
    internal fun `demo app with allow missing from forbidden list - failure`(@TempDir projectDir: File) {
        DependenciesValidationProjectGenerator.generateProject(
            projectDir,
            forbiddenDemoDependencyConnected = false,
            allowForbiddenDemoDependency = true,
        )

        runCheck(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains("Some allow() entries in lib-c/demo/build.gradle are excessive. Remove them:")
            .outputContains("""allow(":heavy-module")""")
    }

    private fun runCheck(projectDir: File, expectFailure: Boolean = false) = gradlew(
        projectDir,
        ":lib-c:demo:validateForbiddenDemoDependencies",
        expectFailure = expectFailure,
        useTestFixturesClasspath = true,
    )
}
