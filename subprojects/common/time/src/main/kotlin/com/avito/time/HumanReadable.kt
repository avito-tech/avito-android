package com.avito.time

import java.util.concurrent.TimeUnit

public fun Long.millisecondsToHumanReadableTime(): String {

    var seconds: Long = TimeUnit.MILLISECONDS.toSeconds(this)
    var minutes: Long = (seconds / 60).apply {
        seconds -= this * 60
    }
    val hours: Long = (minutes / 60).apply {
        minutes -= this * 60
    }

    return buildString {
        if (hours != 0L) {
            append("$hours hour")

            if (hours > 1) {
                append("s")
            }

            append(" ")
        }

        if (minutes != 0L || hours > 0) {
            append("$minutes minute")

            if (minutes != 1L) {
                append("s")
            }

            append(" ")
        }

        append("$seconds second")

        if (seconds != 1L) {
            append("s")
        }
    }
}
