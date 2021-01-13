package com.avito.time

import java.text.DateFormat
import java.util.Date
import java.util.TimeZone

interface TimeProvider {

    val timeZone: TimeZone

    fun isSameDay(date1: Date, date2: Date): Boolean

    fun nowInMillis(): Long

    fun nowInSeconds(): Long

    fun now(): Date

    fun toDate(seconds: Long): Date

    fun formatter(pattern: String): DateFormat
}
