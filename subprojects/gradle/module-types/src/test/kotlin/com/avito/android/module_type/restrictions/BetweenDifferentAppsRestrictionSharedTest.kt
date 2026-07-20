package com.avito.android.module_type.restrictions

import AppA
import AppB
import AppC
import CommonApp
import com.avito.android.module_type.ApplicationDeclaration
import com.avito.android.module_type.FunctionalType
import com.avito.android.module_type.ModuleType
import com.avito.android.module_type.ModuleWithType
import com.avito.android.module_type.Severity
import com.avito.module.configurations.ConfigurationType
import com.google.common.truth.Truth
import org.junit.jupiter.api.Test

class BetweenDifferentAppsRestrictionSharedTest {

    private val restriction = BetweenDifferentAppsRestriction(
        exclusions = emptyList(),
        commonApp = CommonApp,
        sharingApps = setOf(AppA, AppB),
        reason = "Test BetweenDifferentAppsRestriction reason",
        severity = Severity.fail
    )

    private fun module(
        path: String,
        app: ApplicationDeclaration,
        sharedBetweenApps: Boolean = false,
    ) = ModuleWithType(
        path = path,
        type = ModuleType(app, FunctionalType.Impl),
        sharedBetweenApps = sharedBetweenApps,
    )

    private fun isRestricted(module: ModuleWithType, dependency: ModuleWithType): Boolean =
        restriction.isRestricted(module, dependency, ConfigurationType.Main)

    @Test
    fun `sharing app depends on non shared module of another app - restricted`() {
        Truth.assertThat(
            isRestricted(module(":A", AppA), module(":B", AppB))
        ).isEqualTo(true)
    }

    @Test
    fun `sharing app depends on shared - not restricted`() {
        Truth.assertThat(
            isRestricted(
                module(":A", AppA),
                module(":B", AppB, sharedBetweenApps = true)
            )
        ).isEqualTo(false)
    }

    @Test
    fun `other sharing app depends on shared - not restricted`() {
        Truth.assertThat(
            isRestricted(
                module(":A", AppB),
                module(":B", AppA, sharedBetweenApps = true)
            )
        ).isEqualTo(false)
    }

    @Test
    fun `shared depends on shared - not restricted`() {
        Truth.assertThat(
            isRestricted(
                module(":A", AppA, sharedBetweenApps = true),
                module(":B", AppB, sharedBetweenApps = true)
            )
        ).isEqualTo(false)
    }

    @Test
    fun `shared depends on common - not restricted`() {
        Truth.assertThat(
            isRestricted(
                module(":A", AppA, sharedBetweenApps = true),
                module(":B", CommonApp)
            )
        ).isEqualTo(false)
    }

    @Test
    fun `non sharing app depends on shared - restricted`() {
        Truth.assertThat(
            isRestricted(
                module(":A", AppC),
                module(":B", AppA, sharedBetweenApps = true)
            )
        ).isEqualTo(true)
    }

    @Test
    fun `common depends on shared - restricted`() {
        Truth.assertThat(
            isRestricted(
                module(":A", CommonApp),
                module(":B", AppA, sharedBetweenApps = true)
            )
        ).isEqualTo(true)
    }

    @Test
    fun `shared depends on non shared module of same app - not restricted`() {
        Truth.assertThat(
            isRestricted(
                module(":A", AppA, sharedBetweenApps = true),
                module(":B", AppA)
            )
        ).isEqualTo(false)
    }

    @Test
    fun `shared depends on non shared module of another app - restricted`() {
        Truth.assertThat(
            isRestricted(
                module(":A", AppA, sharedBetweenApps = true),
                module(":B", AppB)
            )
        ).isEqualTo(true)
    }
}
