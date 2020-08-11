package com.avito.utils.logging

import java.io.Serializable
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

interface CILoggingFormatter : Serializable {

    fun format(message: String): String
    fun child(tag: String): CILoggingFormatter
}

internal class AppendDateTimeFormatter(
    private val format: DateFormat = SimpleDateFormat.getTimeInstance()
) : CILoggingFormatter {

    override fun format(message: String) = "[${format.format(Date())}] $message"

    override fun child(tag: String): CILoggingFormatter = this
}

internal class AppendPrefixFormatter(
    private val prefix: String
) : CILoggingFormatter {

    override fun format(message: String) = "[$prefix] $message"

    override fun child(tag: String): CILoggingFormatter = AppendPrefixFormatter(
        prefix = "$prefix#$tag"
    )
}
