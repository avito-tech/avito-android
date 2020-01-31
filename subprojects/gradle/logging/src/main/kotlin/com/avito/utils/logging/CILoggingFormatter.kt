package com.avito.utils.logging

class SlackFormatter(
    private val name: String,
    private val buildUrl: String
) : CILoggingFormatter() {

    override fun format(message: String, error: Throwable?): FormattedMessage =
        toFormattedMessage(
            message = message,
            error = error
        ) { messageToFormat ->
            """
*$name* ($buildUrl)

$messageToFormat
""".trimIndent()
        }

    override fun child(tag: String): CILoggingFormatter = this
}
