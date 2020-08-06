package com.avito.android.util

import org.apache.commons.text.TextStringBuilder
import java.io.PrintWriter

/**
 * Prepare stacktrace for report
 */
internal fun Throwable.formatStackTrace(): List<String> {
    val stringBuilder = TextStringBuilder()
    printStackTrace(PrintWriter(stringBuilder.asWriter()))
    return stringBuilder.lines()
}
