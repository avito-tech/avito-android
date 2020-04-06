package com.avito.android.impact

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class ReadModuleIdsKtTest {

    @Test
    fun `readModuleIds - returns emptyMap - for empty input`() {
        val result = readModuleIds(emptySequence())

        assertThat(result).isEmpty()
    }

    @Test
    fun `readModuleIds - returns ids - for multiple diverse inputs`() {
        val result = readModuleIds(
            sequenceOf(
                "ru.avito.reviews",
                "id disclaimer",
                "id negativeIcon",
                "string city_rating",
                "layout rating_districts"
            )
        )

        assertThat(result).containsExactly("disclaimer", "negativeIcon")
    }
}
