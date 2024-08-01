package com.avito.android.module_type.internal

import LibraryModule
import com.avito.android.module_type.ModuleWithType
import com.avito.android.module_type.Severity
import com.avito.android.module_type.restrictions.DependencyRestriction
import com.avito.module.configurations.ConfigurationType.Main
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class ModulesRestrictionsFinderTest {

    private val modules = setOf(
        ModuleDescription(
            module = ModuleWithType(":A", LibraryModule),
            directDependencies = mapOf(Main to setOf(":B"))
        ),
        ModuleDescription(
            module = ModuleWithType(":B", LibraryModule),
            directDependencies = emptyMap()
        )
    )

    private val ignoredRestriction = DependencyRestriction(
        reason = "Stub ignored",
        severity = Severity.ignore
    )

    private val failedRestriction = DependencyRestriction(
        reason = "Stub failed",
        severity = Severity.fail
    )

    @Test
    fun `empty restrictions = success`() {
        val result = violations(
            modules = modules,
            restrictions = emptyList()
        )

        assertThat(result).isEmpty()
    }

    @Test
    fun `only ignored restrictions = success`() {
        val result = violations(
            modules = modules,
            restrictions = listOf(ignoredRestriction)
        )

        assertThat(result).isEmpty()
    }

    @Test
    fun `failed restrictions = one violation`() {
        val result = violations(
            modules = modules,
            restrictions = listOf(failedRestriction)
        )

        assertThat(result).hasSize(1)
        assertThat(result[0].restriction).isEqualTo(failedRestriction)
    }

    @Test
    fun `2 failed restrictions = 2 violations`() {
        val secondFailed = DependencyRestriction(
            reason = "Second failed",
            severity = Severity.fail
        )
        val result = violations(
            modules = modules,
            restrictions = listOf(failedRestriction, secondFailed)
        )

        assertThat(result).hasSize(2)
        assertThat(result[0].restriction).isEqualTo(failedRestriction)
        assertThat(result[1].restriction).isEqualTo(secondFailed)
    }

    private fun violations(
        modules: Set<ModuleDescription>,
        restrictions: List<DependencyRestriction>
    ): List<RestrictionViolation> =
        ModulesRestrictionsFinder(modules, restrictions).violations()
}
