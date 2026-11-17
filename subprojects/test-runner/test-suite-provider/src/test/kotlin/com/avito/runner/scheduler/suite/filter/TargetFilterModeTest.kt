package com.avito.runner.scheduler.suite.filter

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class TargetFilterModeTest {

    @Test
    fun `by target - modes must cover exactly the configured targets`() {
        val targetFilterMode = TargetFilterMode.ByTarget(mapOf("api30" to ChangedTestsFilterMode.CHANGED))

        targetFilterMode.validateTargets(setOf("api30"))
        val missing = assertThrows<IllegalArgumentException> {
            targetFilterMode.validateTargets(setOf("api30", "api36"))
        }
        assertThat(missing).hasMessageThat().contains("missing=[api36]")
        val unknown = assertThrows<IllegalArgumentException> { targetFilterMode.validateTargets(setOf("api36")) }
        assertThat(unknown).hasMessageThat().contains("unknown=[api30]")
    }

    @Test
    fun `by target - empty and blank target declarations are rejected`() {
        assertThrows<IllegalArgumentException> { TargetFilterMode.ByTarget(emptyMap()) }
        assertThrows<IllegalArgumentException> {
            TargetFilterMode.ByTarget(mapOf(" " to ChangedTestsFilterMode.ALL))
        }
    }
}
