package com.avito.time

import java.text.DateFormat
import java.util.Date
import java.util.TimeZone

class StubTimeProvider : TimeProvider {

    private val timeProvider = DefaultTimeProvider()

    override val timeZone: TimeZone = timeProvider.timeZone

    lateinit var now: Date

    override fun isSameDay(date1: Date, date2: Date): Boolean = timeProvider.isSameDay(date1, date2)

    override fun nowInMillis(): Long = timeProvider.nowInMillis()

    override fun nowInSeconds(): Long = timeProvider.nowInSeconds()

    override fun now(): Date = now

    override fun toDate(seconds: Long): Date = timeProvider.toDate(seconds)

    override fun formatter(pattern: String): DateFormat = timeProvider.formatter(pattern)
}
