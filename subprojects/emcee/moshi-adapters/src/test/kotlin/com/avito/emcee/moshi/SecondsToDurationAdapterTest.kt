package com.avito.emcee.moshi

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.addAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalStdlibApi::class)
internal class SecondsToDurationAdapterTest {

    private val moshi = Moshi.Builder()
        .addAdapter(SecondsToDurationAdapter())
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val durationAdapter = moshi.adapter(DurationContainer::class.java)

    @ParameterizedTest
    @MethodSource("dataSet")
    fun serialization(testEntry: TestEntry) {
        val container = DurationContainer(duration = testEntry.duration)
        val json = durationAdapter.toJson(container)
        assertThat(json).isEqualTo("{\"duration\":${testEntry.seconds}}")
    }

    @ParameterizedTest
    @MethodSource("dataSet")
    fun deserialization(testEntry: TestEntry) {
        val json = "{\"duration\":${testEntry.seconds}}"
        val container = durationAdapter.fromJson(json)
        assertThat(container).isEqualTo(DurationContainer(testEntry.duration))
    }

    @JsonClass(generateAdapter = false)
    private data class DurationContainer(
        val duration: Duration
    )

    companion object {

        data class TestEntry(
            val seconds: Double,
            val duration: Duration,
        )

        @JvmStatic
        fun dataSet() = listOf(
            TestEntry(2.0, 2.seconds),
            TestEntry(5.0 * 60, 5.minutes),
            TestEntry(7.0 * 60 * 60, 7.hours),
            TestEntry(10.0 * 24 * 60 * 60, 10.days),
            TestEntry(0.5, 0.5.seconds),
            TestEntry(6.223344, 6.223344.seconds),
        )
    }
}
