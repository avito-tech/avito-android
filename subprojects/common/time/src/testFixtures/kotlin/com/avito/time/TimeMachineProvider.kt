package com.avito.time

import java.util.concurrent.TimeUnit

/**
 * Help to reduce Thread.sleep
 */
class TimeMachineProvider : TimeProvider {

    private var timeShift: Long = 0

    private val defaultTimeProvider = DefaultTimeProvider()

    fun moveForwardOn(time: Long, unit: TimeUnit = TimeUnit.MILLISECONDS) {
        timeShift += unit.toMillis(time)
    }

    fun moveBackOn(time: Long, unit: TimeUnit = TimeUnit.MILLISECONDS) {
        timeShift -= unit.toMillis(time)
    }

    override fun nowInMillis() = defaultTimeProvider.nowInMillis() + timeShift

    override fun nowInSeconds() = defaultTimeProvider.nowInMillis() + TimeUnit.MILLISECONDS.toSeconds(timeShift)

    override fun now() = defaultTimeProvider.toDate(nowInSeconds())

    override fun toDate(seconds: Long) = defaultTimeProvider.toDate(seconds)
}
