package com.avito.android.async_tc_cleaner.internal.reaper

import java.nio.file.Path

internal object FailureSamplesUtil {

    const val MAX: Int = 5

    fun format(path: Path, error: Throwable): String {
        val reason = listOfNotNull(
            error.javaClass.simpleName,
            error.message?.takeIf { it.isNotBlank() },
        ).joinToString(": ")
        return "$path ($reason)"
    }

    fun capped(samples: List<String>): List<String> = samples.take(MAX)
}
