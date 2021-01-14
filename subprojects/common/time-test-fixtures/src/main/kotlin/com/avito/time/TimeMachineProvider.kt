package com.avito.time

import com.avito.logger.StubLoggerFactory
import java.text.DateFormat
import java.util.Date
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * Help to reduce Thread.sleep
 */
class TimeMachineProvider : TimeProvider {

    private val loggerFactory = StubLoggerFactory

    private var timeShift: Long = 0

    private val defaultTimeProvider = DefaultTimeProvider(loggerFactory)

    override val timeZone: TimeZone = defaultTimeProvider.timeZone

    fun moveForwardOn(time: Long, unit: TimeUnit = TimeUnit.MILLISECONDS) {
        timeShift += unit.toMillis(time)
    }

    fun moveBackOn(time: Long, unit: TimeUnit = TimeUnit.MILLISECONDS) {
        timeShift -= unit.toMillis(time)
    }

    override fun isSameDay(date1: Date, date2: Date) = defaultTimeProvider.isSameDay(date1, date2)

    override fun nowInMillis() = defaultTimeProvider.nowInMillis() + timeShift

    override fun nowInSeconds() = defaultTimeProvider.nowInMillis() + TimeUnit.MILLISECONDS.toSeconds(timeShift)

    override fun now() = defaultTimeProvider.toDate(nowInSeconds())

    override fun toDate(seconds: Long) = defaultTimeProvider.toDate(seconds)

    override fun formatter(pattern: String): DateFormat = defaultTimeProvider.formatter(pattern)
}
