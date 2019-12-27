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
