package com.avito.utils

import java.io.PrintWriter
import java.io.StringWriter

fun Throwable.getStackTraceString(): String {
    val stringWriter = StringWriter()
    val printWriter = PrintWriter(stringWriter, false)

    printWriter.use {
        printStackTrace(it)
    }

    return stringWriter.buffer.toString()
}

fun Throwable.getCausesRecursively(): List<Throwable> {
    val causes = mutableListOf<Throwable>()
    var current = this
    while (current.cause != null) {
        val cause = current.cause!!
        causes.add(cause)
        current = cause
    }
    return causes
}
