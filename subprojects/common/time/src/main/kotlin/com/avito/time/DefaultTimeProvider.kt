package com.avito.time

import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

// todo java8 api
class DefaultTimeProvider : TimeProvider {

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
        val millis = seconds * 1000
        return Date(millis)
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
            cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
