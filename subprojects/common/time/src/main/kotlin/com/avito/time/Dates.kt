package com.avito.time

import java.util.Calendar
import java.util.Date

fun Date.isSameDay(otherDate: Date): Boolean {
    return isSameDay(
        Calendar.getInstance().apply { time = this@isSameDay },
        Calendar.getInstance().apply { time = otherDate }
    )
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
        cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
