package com.avito.logger.formatter

import com.avito.logger.LoggingFormatter

public class AppendClassNameFormatter(private val className: String) : LoggingFormatter {

    override fun format(message: String): String = "[$className] $message"
}
