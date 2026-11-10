package com.avito.runner.scheduler.suite.filter

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ImpactAnalysisSelectionTest {

    @Test
    fun `by target - modes must cover exactly the configured targets`() {
        val selection = ImpactAnalysisSelection.ByTarget(mapOf("api30" to ImpactAnalysisMode.CHANGED))

        selection.validateTargets(setOf("api30"))
        val missing = assertThrows<IllegalArgumentException> {
            selection.validateTargets(setOf("api30", "api36"))
        }
        assertThat(missing).hasMessageThat().contains("missing=[api36]")
        val unknown = assertThrows<IllegalArgumentException> { selection.validateTargets(setOf("api36")) }
        assertThat(unknown).hasMessageThat().contains("unknown=[api30]")
    }

    @Test
    fun `by target - empty and blank target declarations are rejected`() {
        assertThrows<IllegalArgumentException> { ImpactAnalysisSelection.ByTarget(emptyMap()) }
        assertThrows<IllegalArgumentException> {
            ImpactAnalysisSelection.ByTarget(mapOf(" " to ImpactAnalysisMode.ALL))
        }
    }
}
