package com.avito.logger.formatter

import com.avito.logger.LoggingFormatter

internal class AppendPrefixFormatter(
    private val prefix: String
) : LoggingFormatter {

    override fun format(message: String) = "[$prefix] $message"
}
