package com.avito.utils.logging

import com.avito.utils.getStackTraceString
import java.io.Serializable
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

abstract class CILoggingFormatter : Serializable {

    abstract fun format(message: String, error: Throwable?): FormattedMessage
    abstract fun child(tag: String): CILoggingFormatter

    data class FormattedMessage(
        val message: String,
        val details: String? = null,
        val cause: Throwable? = null
    )

    protected fun toFormattedMessage(
        message: String,
        error: Throwable?,
        formatter: (String) -> String = { it }
    ) = FormattedMessage(
        message = formatter(message),
        details = error?.getStackTraceString(),
        cause = error
    )
}

object NothingLoggingFormatter : CILoggingFormatter() {

    override fun format(message: String, error: Throwable?): FormattedMessage =
        toFormattedMessage(
            message = message,
            error = error
        )

    override fun child(tag: String): CILoggingFormatter = this
}

class AppendDateTimeFormatter(
    private val format: DateFormat = SimpleDateFormat.getTimeInstance()
) : CILoggingFormatter() {

    override fun format(message: String, error: Throwable?): FormattedMessage =
        toFormattedMessage(
            message = message,
            error = error
        ) { messageToFormat -> "[${format.format(Date())}] $messageToFormat" }

    override fun child(tag: String): CILoggingFormatter = this
}

class AppendPrefixFormatter(
    private val prefix: String
) : CILoggingFormatter() {

    override fun format(message: String, error: Throwable?): FormattedMessage =
        toFormattedMessage(
            message = message,
            error = error
        ) { messageToFormat -> "[$prefix] $messageToFormat" }

    override fun child(tag: String): CILoggingFormatter = AppendPrefixFormatter(
        prefix = "$prefix#$tag"
    )
}
