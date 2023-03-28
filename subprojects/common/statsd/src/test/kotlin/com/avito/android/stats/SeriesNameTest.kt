package com.avito.android.stats

import com.avito.graphite.series.SeriesName
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

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

    @Test
    fun `add tag`() {
        val result = SeriesName.create("one").addTag("tag", "tagValue")

        assertThat(result.toString()).isEqualTo("one;tag=tagValue")
    }

    @Test
    fun `add tags at once`() {
        val result = SeriesName.create("one").addTags(
            mapOf(
                "tag1" to "tagValue",
                "tag2" to "tagValue",
            )
        )

        assertThat(result.toString()).isEqualTo("one;tag1=tagValue;tag2=tagValue")
    }

    @Test
    fun `add tags to existing tags`() {
        val series = SeriesName.create("one")
            .addTag("tag01", "tagValue")
            .addTag("tag02", "tagValue")

        val result = series.addTags(
            mapOf(
                "tag1" to "tagValue",
                "tag2" to "tagValue",
            )
        )

        assertThat(result.toString()).isEqualTo("one;tag01=tagValue;tag02=tagValue;tag1=tagValue;tag2=tagValue")
    }

    @Test
    fun `add tags by two executions`() {
        val result = SeriesName.create("one")
            .addTag("tag1", "tagValue")
            .addTag("tag2", "tagValue")

        assertThat(result.toString()).isEqualTo("one;tag1=tagValue;tag2=tagValue")
    }

    @Test
    fun `add empty tag key - fail`() {
        assertThrows<IllegalArgumentException> {
            SeriesName.create("one")
                .addTag("", "tagValue")
        }
    }

    @Test
    fun `add empty tag value - fail`() {
        assertThrows<IllegalArgumentException> {
            SeriesName.create("one")
                .addTag("t", "")
        }
    }

    @Test
    fun `append series with tags value`() {
        val given = SeriesName.create("given")
        val tags = SeriesName.create().addTag("one", "value")
        val result = given.append(tags)
        assertThat(result.toString()).isEqualTo("given;one=value")
    }
}
