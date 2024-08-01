package com.avito.android.module_type.restrictions

import StubApplication
import com.avito.android.module_type.FunctionalType
import com.avito.android.module_type.ModuleType
import com.avito.android.module_type.ModuleWithType
import com.avito.android.module_type.Severity
import com.avito.android.module_type.restrictions.exclusion.BetweenModulesExclusion
import com.avito.module.configurations.ConfigurationType
import com.google.common.truth.Truth
import org.junit.jupiter.api.Test

class DependencyRestrictionTest {

    private val restriction = object : DependencyRestriction(
        exclusions = listOf(
            BetweenModulesExclusion(
                fromModule = setOf(":A"),
                toDependency = setOf(":B"),
                reason = "Test BetweenModulesExclusion reason"
            )
        )
    ) {
        override val reason: String = "Test reason"
        override val severity: Severity = Severity.fail

        override fun isRestrictedInternal(
            module: ModuleWithType,
            dependency: ModuleWithType,
            configuration: ConfigurationType
        ): Boolean = true
    }

    @Test
    fun `it's exclusion - not restricted`() {
        val result = restriction.isRestricted(
            module = ModuleWithType(
                path = ":A",
                type = ModuleType(StubApplication, FunctionalType.Abstract)
            ),
            dependency = ModuleWithType(
                path = ":B",
                type = ModuleType(StubApplication, FunctionalType.Impl)
            ),
            configuration = ConfigurationType.Main
        )

        Truth.assertThat(result).isEqualTo(false)
    }

    @Test
    fun `it's not exclusion - is restricted internal result`() {
        val result = restriction.isRestricted(
            module = ModuleWithType(
                path = ":A",
                type = ModuleType(StubApplication, FunctionalType.Abstract)
            ),
            dependency = ModuleWithType(
                path = ":C",
                type = ModuleType(StubApplication, FunctionalType.Impl)
            ),
            configuration = ConfigurationType.Main
        )

        Truth.assertThat(result).isEqualTo(true)
    }
}
