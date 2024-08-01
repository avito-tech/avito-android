package com.avito.android.module_type.restrictions

import CommonApp
import StubApplication
import com.avito.android.module_type.FunctionalType
import com.avito.android.module_type.ModuleType
import com.avito.android.module_type.ModuleWithType
import com.avito.android.module_type.Severity
import com.avito.module.configurations.ConfigurationType
import com.google.common.truth.Truth
import org.junit.jupiter.api.Test

class BetweenDifferentAppsRestrictionTest {

    private val restriction = BetweenDifferentAppsRestriction(
        exclusions = emptyList(),
        commonApp = CommonApp,
        reason = "Test BetweenDifferentAppsRestriction reason",
        severity = Severity.fail
    )

    @Test
    fun `module and dependency are same app - not restricted`() {
        val result = restriction.isRestricted(
            module = ModuleWithType(
                path = ":A",
                type = ModuleType(StubApplication, FunctionalType.Impl)
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
    fun `module app and dependency common app - not restricted`() {
        val result = restriction.isRestricted(
            module = ModuleWithType(
                path = ":A",
                type = ModuleType(StubApplication, FunctionalType.Impl)
            ),
            dependency = ModuleWithType(
                path = ":B",
                type = ModuleType(CommonApp, FunctionalType.Impl)
            ),
            configuration = ConfigurationType.Main
        )

        Truth.assertThat(result).isEqualTo(false)
    }

    @Test
    fun `module app and dependency are not same apps - restricted`() {
        val result = restriction.isRestricted(
            module = ModuleWithType(
                path = ":A",
                type = ModuleType(CommonApp, FunctionalType.Impl)
            ),
            dependency = ModuleWithType(
                path = ":B",
                type = ModuleType(StubApplication, FunctionalType.Impl)
            ),
            configuration = ConfigurationType.Main
        )

        Truth.assertThat(result).isEqualTo(true)
    }
}
