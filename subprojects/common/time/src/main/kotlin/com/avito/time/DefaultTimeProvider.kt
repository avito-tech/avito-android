package com.avito.time

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * Used in android runtime < 26, so no java.time API yet
 * consider using ThreeTenABP
 */
class DefaultTimeProvider : TimeProvider {

    override val timeZone: TimeZone = TimeZone.getTimeZone("Europe/Moscow")

    override fun isSameDay(date1: Date, date2: Date): Boolean {
        return isSameDay(
            Calendar.getInstance().apply { time = date1 },
            Calendar.getInstance().apply { time = date2 }
        )
    }

    override fun nowInMillis(): Long = System.currentTimeMillis()

    override fun nowInSeconds(): Long = TimeUnit.MILLISECONDS.toSeconds(nowInMillis())

    override fun now(): Date = toDate(nowInSeconds())

    override fun toDate(seconds: Long): Date {
        val millis = TimeUnit.SECONDS.toMillis(seconds)
        return Date(millis)
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
            cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    override fun formatter(pattern: String): DateFormat {
        return SimpleDateFormat(pattern, Locale.getDefault()).apply {
            timeZone = this@DefaultTimeProvider.timeZone
        }
    }
}
