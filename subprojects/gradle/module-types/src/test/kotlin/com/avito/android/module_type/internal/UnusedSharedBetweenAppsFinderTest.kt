package com.avito.android.module_type.internal

import AppA
import AppB
import AppC
import CommonApp
import com.avito.android.module_type.ApplicationDeclaration
import com.avito.android.module_type.FunctionalType
import com.avito.android.module_type.ModuleType
import com.avito.android.module_type.ModuleWithType
import com.avito.module.configurations.ConfigurationType
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class UnusedSharedBetweenAppsFinderTest {

    @Test
    fun `no flags - nothing unused`() {
        assertThat(
            unused(
                moduleDescription(":A", AppA, deps = listOf(":B")),
                moduleDescription(":B", AppB),
            )
        ).isEmpty()
    }

    @Test
    fun `direct cross-app consumer from sharing app - justified`() {
        assertThat(
            unused(
                moduleDescription(":A", AppA, deps = listOf(":B")),
                moduleDescription(":B", AppB, shared = true),
            )
        ).isEmpty()
    }

    @Test
    fun `only same-app consumer - unused`() {
        assertThat(
            unused(
                moduleDescription(":A", AppA, deps = listOf(":B")),
                moduleDescription(":B", AppA, shared = true),
            )
        ).containsExactly(":B")
    }

    @Test
    fun `no consumer at all - unused`() {
        assertThat(
            unused(
                moduleDescription(":B", AppB, shared = true),
            )
        ).containsExactly(":B")
    }

    @Test
    fun `common module flag is never justified`() {
        assertThat(
            unused(
                moduleDescription(":A", AppA, deps = listOf(":M")),
                moduleDescription(":M", CommonApp, shared = true),
            )
        ).containsExactly(":M")
    }

    @Test
    fun `common module flag is redundant even with a direct cross-app consumer`() {
        // A common module is reachable by every app without a flag, so the flag is redundant even
        // when a module of another app depends on it directly. It is reported as redundant, not as
        // "no cross-app consumer".
        val result = find(
            moduleDescription(":A", AppA, deps = listOf(":M")),
            moduleDescription(":M", CommonApp, shared = true),
        )
        assertThat(result.redundantCommonModules).containsExactly(":M")
        assertThat(result.unjustifiedModules).isEmpty()
    }

    @Test
    fun `common module flag with no consumer is redundant not unjustified`() {
        val result = find(
            moduleDescription(":M", CommonApp, shared = true),
        )
        assertThat(result.redundantCommonModules).containsExactly(":M")
        assertThat(result.unjustifiedModules).isEmpty()
    }

    @Test
    fun `consumer from non sharing app does not justify`() {
        assertThat(
            unused(
                moduleDescription(":C", AppC, deps = listOf(":M")),
                moduleDescription(":M", AppA, shared = true),
            )
        ).containsExactly(":M")
    }

    @Test
    fun `shared consumer of another app justifies`() {
        assertThat(
            unused(
                moduleDescription(":B", AppB, deps = listOf(":A")),
                moduleDescription(":A", AppA, shared = true, deps = listOf(":M")),
                moduleDescription(":M", AppB, shared = true),
            )
        ).isEmpty()
    }

    @Test
    fun `flagged same-app shared consumer does not justify`() {
        assertThat(
            unused(
                moduleDescription(":C", AppA, shared = true, deps = listOf(":M")),
                moduleDescription(":M", AppA, shared = true),
            )
        ).containsExactly(":C", ":M")
    }

    @Test
    fun `flagged common consumer does not justify`() {
        assertThat(
            unused(
                moduleDescription(":Cm", CommonApp, shared = true, deps = listOf(":M")),
                moduleDescription(":M", AppA, shared = true),
            )
        ).containsExactly(":Cm", ":M")
    }

    @Test
    fun `shared module consumed only by an unused shared module of another app is still justified`() {
        // :A is unused (nobody consumes it), but the edge :A -> :M is a real cross-app edge that the
        // flag on :M makes legal (:A's app is a sharing app). Removing :M's flag would make the
        // restriction check fail on :A -> :M, so :M is load-bearing and must NOT be reported here.
        // Only :A is reported; once its flag (or the :A -> :M edge) is gone, :M is re-evaluated.
        assertThat(
            unused(
                moduleDescription(":A", AppA, shared = true, deps = listOf(":M")),
                moduleDescription(":M", AppB, shared = true),
            )
        ).containsExactly(":A")
    }

    @Test
    fun `transitive cross-app reachability does not justify`() {
        assertThat(
            unused(
                moduleDescription(":B", AppB, deps = listOf(":X")),
                moduleDescription(":X", AppA, deps = listOf(":M")),
                moduleDescription(":M", AppA, shared = true),
            )
        ).containsExactly(":M")
    }

    @Test
    fun `multiple unused flags are returned sorted`() {
        assertThat(
            unused(
                moduleDescription(":Z", AppA, shared = true),
                moduleDescription(":Y", AppB, shared = true),
                moduleDescription(":A", AppA, deps = listOf(":Justified")),
                moduleDescription(":Justified", AppB, shared = true),
            )
        ).containsExactly(":Y", ":Z").inOrder()
    }

    private fun unused(vararg modules: ModuleDescription): List<String> =
        find(*modules).let { (it.unjustifiedModules + it.redundantCommonModules).sorted() }

    private fun find(vararg modules: ModuleDescription): UnusedSharedBetweenAppsFinder.UnusedFlags =
        UnusedSharedBetweenAppsFinder(
            moduleDescriptions = modules.toSet(),
            commonApp = CommonApp,
            sharingApps = setOf(AppA, AppB),
        ).findUnusedFlags()

    private fun moduleDescription(
        path: String,
        app: ApplicationDeclaration,
        shared: Boolean = false,
        deps: List<String> = emptyList(),
    ) = ModuleDescription(
        module = ModuleWithType(path, ModuleType(app, FunctionalType.Impl), sharedBetweenApps = shared),
        directDependencies = if (deps.isEmpty()) {
            emptyMap()
        } else {
            mapOf(ConfigurationType.Main to deps.toSet())
        }
    )
}
