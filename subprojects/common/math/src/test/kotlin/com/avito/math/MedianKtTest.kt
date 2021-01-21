package com.avito.math

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class MedianKtTest {

    @Test
    fun `odd number of elements`() {
        val elements = listOf(5, 10, 5)

        val result = elements.median()

        assertThat(result).isEqualTo(5)
    }

    @Test
    fun `even number of elements`() {
        val elements = listOf(5, 10, 5, 25)

        val result = elements.median()

        assertThat(result).isEqualTo(7.5)
    }

    @Test
    fun `one element`() {
        val elements = listOf(10)

        val result = elements.median()

        assertThat(result).isEqualTo(10)
    }

    @Test
    fun `no elements`() {
        val elements = emptyList<Int>()

        assertThrows<IllegalArgumentException> {
            elements.median()
        }
    }
}
