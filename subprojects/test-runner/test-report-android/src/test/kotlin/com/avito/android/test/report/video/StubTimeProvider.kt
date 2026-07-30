package com.avito.android.test.report.video

import com.avito.time.TimeProvider
import java.time.Instant
import java.util.Date

/**
 * Time moves forward by [stepMs] on each call to make measured durations predictable
 */
internal class StubTimeProvider(
    private val stepMs: Long = 100L
) : TimeProvider {

    private var now: Long = 0

    override fun nowInMillis(): Long {
        val current = now
        now += stepMs
        return current
    }

    override fun nowInstant(): Instant = Instant.ofEpochMilli(now)

    override fun nowInSeconds(): Long = now / 1000

    override fun now(): Date = Date(now)

    override fun toDate(seconds: Long): Date = Date(seconds * 1000)
}
