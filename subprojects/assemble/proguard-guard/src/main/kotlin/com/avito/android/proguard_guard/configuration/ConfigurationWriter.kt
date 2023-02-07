package com.avito.android.proguard_guard.configuration

import proguard.Configuration
import proguard.ConfigurationWriter
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

/**
 * @return sorted content of configuration file as List<String>
 */
internal fun Configuration.writeTo(file: File): List<String> {
    val stringWriter = StringWriter()
    val printWriter = PrintWriter(stringWriter)
    ConfigurationWriter(printWriter).write(this)
    val configurationString = stringWriter.toString()

    val filteredLines: List<String> = configurationString.splitToSequence('\n')
        .filterNot { it.isBlank() || it.startsWith('#') }
        .toList()

    file.printWriter().use { filePrintWriter ->
        filteredLines.forEach { line ->
            filePrintWriter.println(line)
        }
        filePrintWriter.flush()
    }

    return filteredLines
}
