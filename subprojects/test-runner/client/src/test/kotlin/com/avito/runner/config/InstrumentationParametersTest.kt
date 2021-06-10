package com.avito.runner.config

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class InstrumentationParametersTest {

    @Test
    fun `instrumentationParameters - equals - for equal data`() {
        val one = InstrumentationParameters(mapOf("1" to "2", "one" to "two"))
        val two = InstrumentationParameters(mapOf("one" to "two", "1" to "2"))
        assertThat(one).isEqualTo(two)
    }

    @Test
    fun `instrumentationParameters - equals - for equal data passed via applyParameters`() {
        val one = InstrumentationParameters(mapOf("1" to "2", "one" to "two"))
        val two = InstrumentationParameters(mapOf("one" to "two")).applyParameters(mapOf("1" to "2"))
        assertThat(one).isEqualTo(two)
    }
}
