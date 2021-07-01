package com.avito.time

import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Help to reduce Thread.sleep
 */
public class TimeMachineProvider : TimeProvider {

    private var timeShift: Long = 0

    private val defaultTimeProvider = DefaultTimeProvider()

    public fun moveForwardOn(time: Long, unit: TimeUnit = TimeUnit.MILLISECONDS) {
        timeShift += unit.toMillis(time)
    }

    public fun moveBackOn(time: Long, unit: TimeUnit = TimeUnit.MILLISECONDS) {
        timeShift -= unit.toMillis(time)
    }

    override fun nowInMillis(): Long = defaultTimeProvider.nowInMillis() + timeShift

    override fun nowInSeconds(): Long = defaultTimeProvider.nowInMillis() + TimeUnit.MILLISECONDS.toSeconds(timeShift)

    override fun now(): Date = defaultTimeProvider.toDate(nowInSeconds())

    override fun toDate(seconds: Long): Date = defaultTimeProvider.toDate(seconds)
}
