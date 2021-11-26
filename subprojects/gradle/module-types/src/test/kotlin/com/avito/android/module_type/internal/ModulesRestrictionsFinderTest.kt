package com.avito.android.module_type.internal

import BetweenModuleTypes
import DependentModule
import FeatureModule
import LibraryModule
import TestModule
import ToTestModule
import com.avito.android.module_type.DependencyRestriction
import com.avito.android.module_type.ModuleWithType
import com.avito.module.configurations.ConfigurationType.Main
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ModulesRestrictionsFinderTest {

    @Test
    fun `finds restricted dependency - main configuration`() {
        val modules = setOf(
            ModuleDescription(
                module = ModuleWithType(":A", LibraryModule),
                directDependencies = mapOf(Main to setOf(":B", ":C"))
            ),
            ModuleDescription(
                module = ModuleWithType(":B", LibraryModule),
                directDependencies = emptyMap()
            ),
            ModuleDescription(
                module = ModuleWithType(":C", FeatureModule),
                directDependencies = emptyMap()
            )
        )
        val restriction = DependencyRestriction(
            matcher = BetweenModuleTypes(module = LibraryModule, dependency = FeatureModule),
        )
        val restrictions = listOf(restriction)

        val violations = violations(modules, restrictions)

        assertWithMessage("Found one violation")
            .that(violations).hasSize(1)

        val violation = violations.first()

        assertThat(violation.module).isEqualTo(ModuleWithType(":A", LibraryModule))
        assertThat(violation.dependency).isEqualTo(ModuleWithType(":C", FeatureModule))
        assertThat(violation.restriction).isEqualTo(restriction)
    }

    @Test
    fun `finds restricted dependency - test configuration`() {
        val modules = setOf(
            ModuleDescription(
                module = ModuleWithType(":lib", LibraryModule),
                directDependencies = mapOf(Main to setOf(":test-fixtures"))
            ),
            ModuleDescription(
                module = ModuleWithType(":test-fixtures", TestModule),
                directDependencies = emptyMap()
            ),
        )
        val restriction = DependencyRestriction(ToTestModule())
        val restrictions = listOf(restriction)

        val violations = violations(modules, restrictions)

        assertWithMessage("Found one violation")
            .that(violations).hasSize(1)

        val violation = violations.first()

        assertThat(violation.module).isEqualTo(ModuleWithType(":lib", LibraryModule))
        assertThat(violation.dependency).isEqualTo(ModuleWithType(":test-fixtures", TestModule))
        assertThat(violation.restriction).isEqualTo(restriction)
    }

    @Test
    fun `filters excluded dependencies`() {
        val modules = setOf(
            ModuleDescription(
                module = ModuleWithType(":A", LibraryModule),
                directDependencies = mapOf(Main to setOf(":B"))
            ),
            ModuleDescription(
                module = ModuleWithType(":B", LibraryModule),
                directDependencies = emptyMap()
            )
        )
        val restriction = DependencyRestriction(
            matcher = BetweenModuleTypes(module = LibraryModule, dependency = LibraryModule),
            exclusions = setOf(DependentModule(":B"))
        )
        val restrictions = listOf(restriction)

        val violations = violations(modules, restrictions)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `failure - no module description for dependency`() {
        val modules = setOf(
            ModuleDescription(
                module = ModuleWithType(":A", LibraryModule),
                directDependencies = mapOf(Main to setOf(":MISSED_MODULE"))
            )
        )
        val error = assertThrows<RuntimeException> {
            violations(modules, emptyList())
        }
        assertThat(error).hasMessageThat()
            .contains("Not found module description for :MISSED_MODULE")
    }

    private fun violations(
        modules: Set<ModuleDescription>,
        restrictions: List<DependencyRestriction>
    ): List<RestrictionViolation> =
        ModulesRestrictionsFinder(modules, restrictions).violations()
}
