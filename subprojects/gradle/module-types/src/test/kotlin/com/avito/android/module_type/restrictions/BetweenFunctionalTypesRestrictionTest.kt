package com.avito.android.module_type.restrictions

import StubApplication
import com.avito.android.module_type.FunctionalType
import com.avito.android.module_type.ModuleType
import com.avito.android.module_type.ModuleWithType
import com.avito.android.module_type.Severity
import com.avito.module.configurations.ConfigurationType.Detekt
import com.avito.module.configurations.ConfigurationType.Main
import com.google.common.truth.Truth
import org.junit.jupiter.api.Test

class BetweenFunctionalTypesRestrictionTest {

    private val restriction = BetweenFunctionalTypesRestriction(
        fromType = FunctionalType.Impl,
        allowedTypesByConfiguration = mapOf(Main to setOf(FunctionalType.Public)),
        reason = "Test reason",
        severity = Severity.fail,
        exclusions = emptyList()
    )

    @Test
    fun `module type is not the same as in the restriction - not restricted`() {
        val result = restriction.isRestricted(
            module = ModuleWithType(
                path = ":A",
                type = ModuleType(StubApplication, FunctionalType.Abstract)
            ),
            dependency = ModuleWithType(
                path = ":B",
                type = ModuleType(StubApplication, FunctionalType.Impl)
            ),
            configuration = Main
        )

        Truth.assertThat(result).isEqualTo(false)
    }

    @Test
    fun `module type is the same as in the restriction and dependency type is in allowed - not restricted`() {
        val result = restriction.isRestricted(
            module = ModuleWithType(
                path = ":A",
                type = ModuleType(StubApplication, FunctionalType.Impl)
            ),
            dependency = ModuleWithType(
                path = ":B",
                type = ModuleType(StubApplication, FunctionalType.Public)
            ),
            configuration = Main
        )

        Truth.assertThat(result).isEqualTo(false)
    }

    @Test
    fun `module type is the same as in the restriction and dependency type is not in allowed - restricted`() {
        val result = restriction.isRestricted(
            module = ModuleWithType(
                path = ":A",
                type = ModuleType(StubApplication, FunctionalType.Impl)
            ),
            dependency = ModuleWithType(
                path = ":B",
                type = ModuleType(StubApplication, FunctionalType.Abstract)
            ),
            configuration = Main
        )

        Truth.assertThat(result).isEqualTo(true)
    }

    @Test
    fun `allowedTypes for configuration are not set - restricted`() {
        val result = restriction.isRestricted(
            module = ModuleWithType(
                path = ":A",
                type = ModuleType(StubApplication, FunctionalType.Impl)
            ),
            dependency = ModuleWithType(
                path = ":B",
                type = ModuleType(StubApplication, FunctionalType.Abstract)
            ),
            configuration = Detekt
        )

        Truth.assertThat(result).isEqualTo(true)
    }
}
