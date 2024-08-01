package com.avito.android.module_type.restrictions.exclusion

import StubApplication
import com.avito.android.module_type.FunctionalType
import com.avito.android.module_type.ModuleType
import com.avito.android.module_type.ModuleWithType
import com.avito.module.configurations.ConfigurationType
import com.google.common.truth.Truth
import org.junit.jupiter.api.Test

class BetweenModulesExclusionTest {

    private val exclusion = BetweenModulesExclusion(
        fromModule = setOf(":A"),
        toDependency = setOf(":B"),
        reason = "Test reason"
    )

    @Test
    fun `module in fromModule and dependency in toDependency - exclusion`() {
        val result = exclusion.isExclusion(
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

        Truth.assertThat(result).isTrue()
    }

    @Test
    fun `module in fromModule and dependency not in toDependency - not exclusion`() {
        val result = exclusion.isExclusion(
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

        Truth.assertThat(result).isFalse()
    }

    @Test
    fun `module not in fromModule and dependency in toDependency - not exclusion`() {
        val result = exclusion.isExclusion(
            module = ModuleWithType(
                path = ":C",
                type = ModuleType(StubApplication, FunctionalType.Abstract)
            ),
            dependency = ModuleWithType(
                path = ":B",
                type = ModuleType(StubApplication, FunctionalType.Impl)
            ),
            configuration = ConfigurationType.Main
        )

        Truth.assertThat(result).isFalse()
    }
}
