package com.avito.time

import java.time.Duration
import java.time.Instant
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Help to reduce Thread.sleep
 */
public class TimeMachineProvider : TimeProvider {

    public var now: Long = 0

    private val defaultTimeProvider = DefaultTimeProvider()

    public fun moveForwardOn(time: Long, unit: TimeUnit = TimeUnit.MILLISECONDS) {
        now += unit.toMillis(time)
    }

    public fun moveForwardBy(duration: Duration) {
        now += duration.toMillis()
    }

    public fun moveBackOn(time: Long, unit: TimeUnit = TimeUnit.MILLISECONDS) {
        now -= unit.toMillis(time)
    }

    public fun moveBackBy(duration: Duration) {
        now -= duration.toMillis()
    }

    override fun nowInMillis(): Long = now

    override fun nowInstant(): Instant = Instant.ofEpochMilli(now)

    override fun nowInSeconds(): Long = Instant.ofEpochMilli(now).epochSecond

    override fun now(): Date = Date(now)

    override fun toDate(seconds: Long): Date = defaultTimeProvider.toDate(seconds)
}
