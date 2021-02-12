package com.avito.android.stats

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class SeriesNameTest {

    @Test
    fun `create series name - success - multiple parts`() {
        val result = SeriesName.create("one", "two", "three")

        assertThat(result.toString()).isEqualTo("one.two.three")
    }

    @Test
    fun `create series name - undesired behavior - multiple parts`() {
        val result = SeriesName.create("one", "two.three")

        assertThat(result.toString()).isEqualTo("one.two_three")
    }

    @Test
    fun `create series name - success - single part`() {
        val result = SeriesName.create("one")

        assertThat(result.toString()).isEqualTo("one")
    }

    @Test
    fun `create series name - keeps underscore`() {
        val result = SeriesName.create("one_a")

        assertThat(result.toString()).isEqualTo("one_a")
    }

    @Test
    fun `create series name - keeps minus`() {
        val result = SeriesName.create("one-a")

        assertThat(result.toString()).isEqualTo("one-a")
    }

    @Test
    fun `create series name - replaces unsupported symbol (percent) with underscore`() {
        val result = SeriesName.create("one%a")

        assertThat(result.toString()).isEqualTo("one_a")
    }

    @Test
    fun `create series name - replaces multiple unsupported symbols with single underscore`() {
        val result = SeriesName.create("one%%#$#!a")

        assertThat(result.toString()).isEqualTo("one_a")
    }

    @Test
    fun `create series name - success - from multipart`() {
        val result = SeriesName.create("one.two.three", multipart = true)

        assertThat(result.toString()).isEqualTo("one.two.three")
    }

    @Test
    fun `append series name - success - multipart`() {
        val result = SeriesName.create("one").append("two.three", multipart = true)

        assertThat(result.toString()).isEqualTo("one.two.three")
    }

    @Test
    fun `append series name - undesired behavior - multipart`() {
        val result = SeriesName.create("one").append("two.three", multipart = false)

        assertThat(result.toString()).isEqualTo("one.two_three")
    }

    @Test
    fun `append series name - success`() {
        val result = SeriesName.create("one").append(SeriesName.create("two"))

        assertThat(result.toString()).isEqualTo("one.two")
    }

    @Test
    fun `prefix series name - success - multipart`() {
        val result = SeriesName.create("one").prefix(SeriesName.create("minus-one", "zero"))

        assertThat(result.toString()).isEqualTo("minus-one.zero.one")
    }
}
