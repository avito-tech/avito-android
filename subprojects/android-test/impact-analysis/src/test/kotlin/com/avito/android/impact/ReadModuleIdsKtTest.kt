package com.avito.android.impact

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class ReadModuleIdsKtTest {

    @Test
    fun `readModuleIds - returns emptyMap - for empty input`() {
        val result = readModuleIds(emptyMap())

        assertThat(result).isEmpty()
    }

    @Test
    fun `readModuleIds - returns ids - for multiple diverse inputs`() {
        val result = readModuleIds(
            mapOf(
                "libA" to sequenceOf(
                    "ru.avito.reviews",
                    "id disclaimer",
                    "id negativeIcon",
                    "string city_rating",
                    "layout rating_districts"
                ),
                "libB" to sequenceOf(
                    "ru.avito.adverts",
                    "id sorting",
                    "layout parameter_score",
                    "string category"
                ),
                "libC" to sequenceOf(
                    "ru.avito.category",
                    "layout score",
                    "string category"
                )
            )
        )

        val mappedForAssertion = result.map { (key, value) ->
            key to value.joinToString(",")
        }.toMap()

        assertThat(mappedForAssertion).containsExactlyEntriesIn(
            mapOf(
                "libA" to "disclaimer,negativeIcon",
                "libB" to "sorting",
                "libC" to ""
            )
        )
    }
}
