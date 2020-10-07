package com.avito.android.util

import org.apache.commons.text.TextStringBuilder
import java.io.PrintWriter

/**
 * Get stacktrace as list of lines from throwable
 */
fun Throwable.formatStackTrace(): List<String> {
    val stringBuilder = TextStringBuilder()
    printStackTrace(PrintWriter(stringBuilder.asWriter()))
    return stringBuilder.lines()
}
