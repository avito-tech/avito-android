package com.avito.test.summary.analysis

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FailureMessageNormalizerTest {

    @Test
    fun simple_regex_replacement() {
        val normalizer = RegexFailureMessageNormalizer(
            regex = Regex("Object@[0-9a-f]+"),
            replacement = ""
        )

        val sample = "Error on Object@123afd43"

        assertEquals(
            "Error on ",
            normalizer.normalize(sample)
        )
    }

    @Test
    fun regex_with_pattern_replacement() {
        val normalizer = RegexToPatternMessageNormalizer(
            regex = Regex("Parameter specified as non-null is null.+parameter (.+)"),
            pattern = "Параметр {1} == null"
        )

        val sample = "Parameter specified as non-null is null: method:foo.bar, parameter paramName"

        assertEquals(
            "Параметр paramName == null",
            normalizer.normalize(sample)
        )
    }

    @Test
    fun `no views in hierarchy - replaced with short message with view id`() {
        val verdict = "[test run] В [testCase] Не удалось выполнить шаг No views in hierarchy found matching: " +
            "with id: com.avito.android.dev:id/advert_duplicate_screen_root\\n\\nView Hiera"

        val result = normalize(verdict)

        assertThat(result).isEqualTo("Не найдена view в иерархии: id/advert_duplicate_screen_root")
    }
}
