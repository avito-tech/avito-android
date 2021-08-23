package com.avito.math

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PercentKtTest {

    @Test
    fun `percent of int - round is correct - no decimal`() {
        assertThat(4.percentOf(8).toInt()).isEqualTo(50)
    }

    @Test
    fun `percent of int - round is correct - decimal`() {
        assertThat(5.percentOf(8).toInt()).isEqualTo(62)
    }

    @Test
    fun `percent of int - two decimal string is correct - no decimal`() {
        assertThat(4.percentOf(8).twoDigitsString()).isEqualTo("50%")
    }

    @Test
    fun `percent of int - two decimal string is correct - decimal`() {
        assertThat(5.percentOf(8).twoDigitsString()).isEqualTo("62.5%")
    }

    @Test
    fun `double percent - two decimal string is correct - decimal`() {
        assertThat(0.2345.fromZeroToOnePercent().twoDigitsString()).isEqualTo("23.45%")
    }

    @Test
    fun `float percent - two decimal string is correct - decimal`() {
        assertThat(0.2345F.fromZeroToOnePercent().twoDigitsString()).isEqualTo("23.45%")
    }

    @Test
    fun `percent - throws exception - not in range`() {
        assertThrows<IllegalArgumentException> {
            23.45.fromZeroToOnePercent()
        }
    }
}
