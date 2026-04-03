package com.avito.android.string_transform.internal.task.mapping

import com.avito.android.string_transform.internal.rules.NormalizedRule
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class MappingTextTransformerTest {

    private val transformer = MappingTextTransformer()

    @Test
    fun `mapping text transformer - applies rules sequentially`() {
        val result = transformer.applyRules(
            text = "com.example.a -> b:",
            rules = listOf(
                NormalizedRule(from = "a", to = "b"),
                NormalizedRule(from = "b", to = "c"),
            ),
        )

        assertThat(result).isEqualTo("com.excmple.c -> c:")
    }

    @Test
    fun `mapping text transformer - applies overlapping chained rules in order`() {
        val result = transformer.applyRules(
            text = "samplevalue -> a:",
            rules = listOf(
                NormalizedRule(from = "sample", to = "changed"),
                NormalizedRule(from = "changedvalue", to = "finalvalue"),
            ),
        )

        assertThat(result).isEqualTo("finalvalue -> a:")
    }
}
