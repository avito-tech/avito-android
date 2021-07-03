package com.avito.utils

import org.apache.commons.text.TextStringBuilder
import java.io.PrintWriter

public fun Throwable.getCausesRecursively(): List<Throwable> {
    val causes = mutableListOf<Throwable>()
    var current = this
    while (current.cause != null) {
        val cause = current.cause!!
        causes.add(cause)
        current = cause
    }
    return causes
}

public fun Throwable.stackTraceToList(): List<String> {
    val stringBuilder = TextStringBuilder()
    printStackTrace(PrintWriter(stringBuilder.asWriter()))
    return stringBuilder.lines()
}
