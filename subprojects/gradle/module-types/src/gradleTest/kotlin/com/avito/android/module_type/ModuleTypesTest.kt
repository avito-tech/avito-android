package com.avito.android.module_type

import FeatureModule
import LibraryModule
import TestModule
import com.avito.android.module_type.ModuleTypesProjectGenerator.Constraint
import com.avito.android.module_type.ModuleTypesProjectGenerator.Dependency
import com.avito.module.configurations.ConfigurationType
import com.avito.test.gradle.dependencies.GradleDependency.Safe.CONFIGURATION.IMPLEMENTATION
import com.avito.test.gradle.dependencies.GradleDependency.Safe.CONFIGURATION.TEST_IMPLEMENTATION
import org.junit.jupiter.api.Test

internal class ModuleTypesTest : BaseModuleTypesTest() {

    @Test
    fun `no forbidden dependencies - success`() {
        givenProject(
            moduleType = LibraryModule,
            dependency = Dependency(LibraryModule),
            constraint = Constraint(module = LibraryModule, dependency = FeatureModule)
        )

        val result = runCheck(projectDir)

        result.assertThat().buildSuccessful()
    }

    @Test
    fun `forbidden dependency in implementation - failure`() {
        givenProject(
            moduleType = LibraryModule,
            dependency = Dependency(FeatureModule),
            constraint = Constraint(module = LibraryModule, dependency = FeatureModule)
        )

        val result = runCheck(projectDir, expectFailure = true)

        result.assertThat().outputContains(briefErrorMessage)
        result.assertThat().outputContains(
            """
            module :A (LibraryModule) depends on module :B (FeatureModule)
            It violates a constraint: module of type LibraryModule depends on a module of type FeatureModule
            """.trimIndent()
        )
    }

    @Test
    fun `forbidden dependency in implementation - success - warning severity`() {
        givenProject(
            severity = Severity.warning,
            moduleType = LibraryModule,
            dependency = Dependency(FeatureModule),
            constraint = Constraint(module = LibraryModule, dependency = FeatureModule)
        )

        val result = runCheck(projectDir, expectFailure = false)

        result.assertThat().outputContains(briefErrorMessage)
    }

    @Test
    fun `forbidden test dependency in implementation - failure`() {
        givenProject(
            moduleType = LibraryModule,
            dependency = Dependency(TestModule, IMPLEMENTATION),
            constraint = Constraint(
                module = LibraryModule,
                dependency = TestModule,
                configuration = ConfigurationType.Main
            )
        )

        val result = runCheck(projectDir, expectFailure = true)

        result.assertThat().outputContains(briefErrorMessage)
    }

    @Test
    fun `test dependency in tests - success`() {
        givenProject(
            moduleType = LibraryModule,
            dependency = Dependency(TestModule, TEST_IMPLEMENTATION),
            constraint = Constraint(
                module = LibraryModule,
                dependency = TestModule,
                configuration = ConfigurationType.Main
            )
        )

        val result = runCheck(projectDir, expectFailure = false)

        result.assertThat().buildSuccessful()
    }

    @Test
    fun `success - module is excluded`() {
        givenProject(
            moduleType = LibraryModule,
            dependency = Dependency(FeatureModule),
            constraint = Constraint(module = LibraryModule, dependency = FeatureModule),
            exclusions = setOf(":B")
        )

        val result = runCheck(projectDir, expectFailure = false)

        result.assertThat().outputDoesNotContain(briefErrorMessage)
    }

    @Test
    fun `success - checks are ignored`() {
        givenProject(
            severity = Severity.ignore,
            moduleType = LibraryModule,
            dependency = Dependency(FeatureModule),
            constraint = Constraint(module = LibraryModule, dependency = FeatureModule)
        )

        val result = runCheck(projectDir, expectFailure = false)

        result.assertThat().outputDoesNotContain(briefErrorMessage)
    }
}

private const val briefErrorMessage = "Found forbidden dependencies between modules"
