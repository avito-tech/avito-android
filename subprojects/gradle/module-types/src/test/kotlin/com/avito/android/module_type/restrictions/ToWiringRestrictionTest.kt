package com.avito.android.module_type.restrictions

import StubApplication
import com.avito.android.module_type.FunctionalType
import com.avito.android.module_type.ModuleType
import com.avito.android.module_type.ModuleWithType
import com.avito.android.module_type.Severity
import com.avito.module.configurations.ConfigurationType
import com.google.common.truth.Truth
import org.junit.jupiter.api.Test

class ToWiringRestrictionTest {
    private val restriction = ToWiringRestriction(
        exclusions = emptyList(),
        reason = "Test reason",
        severity = Severity.fail,
    )

    @Test
    fun `module type is not Wiring - not restricted`() {
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
    fun `module type is ImplWiring and dependency is not Impl - restricted`() {
        val result = restriction.isRestricted(
            module = ModuleWithType(
                path = ":A",
                type = ModuleType(StubApplication, FunctionalType.ImplWiring)
            ),
            dependency = ModuleWithType(
                path = ":B",
                type = ModuleType(StubApplication, FunctionalType.Abstract)
            ),
            configuration = ConfigurationType.Main
        )

        Truth.assertThat(result).isEqualTo(true)
    }

    @Test
    fun `module type is FakeWiring and dependency is not Fake - restricted`() {
        val result = restriction.isRestricted(
            module = ModuleWithType(
                path = ":A",
                type = ModuleType(StubApplication, FunctionalType.FakeWiring)
            ),
            dependency = ModuleWithType(
                path = ":B",
                type = ModuleType(StubApplication, FunctionalType.Abstract)
            ),
            configuration = ConfigurationType.Main
        )

        Truth.assertThat(result).isEqualTo(true)
    }

    @Test
    fun `module type is FakeWiring and dependency is Fake and different logic module - restricted`() {
        val result = restriction.isRestricted(
            module = ModuleWithType(
                path = ":logic1:A",
                type = ModuleType(StubApplication, FunctionalType.FakeWiring)
            ),
            dependency = ModuleWithType(
                path = ":logic2:B",
                type = ModuleType(StubApplication, FunctionalType.Fake)
            ),
            configuration = ConfigurationType.Main
        )

        Truth.assertThat(result).isEqualTo(true)
    }

    @Test
    fun `module type is FakeWiring and dependency is Fake and same logic module - not restricted`() {
        val result = restriction.isRestricted(
            module = ModuleWithType(
                path = ":logic1:A",
                type = ModuleType(StubApplication, FunctionalType.FakeWiring)
            ),
            dependency = ModuleWithType(
                path = ":logic1:B",
                type = ModuleType(StubApplication, FunctionalType.Fake)
            ),
            configuration = ConfigurationType.Main
        )

        Truth.assertThat(result).isEqualTo(false)
    }
}
