package com.avito.utils.logging

import java.io.File
import java.io.Serializable

abstract class CILoggingDestination : Serializable {

    abstract fun write(formattedMessage: CILoggingFormatter.FormattedMessage)
    abstract fun child(tag: String): CILoggingDestination

    protected fun CILoggingFormatter.FormattedMessage.toMessage(): String {
        val detailsMessage = if (details != null) {
            "\n$details"
        } else {
            ""
        }

        return message + detailsMessage
    }
}

object StdoutDestination : CILoggingDestination() {

    override fun write(formattedMessage: CILoggingFormatter.FormattedMessage) {
        println(formattedMessage.message)
        formattedMessage.cause?.let { throwable ->
            println(throwable.message)
        }
    }

    override fun child(tag: String): CILoggingDestination = this
}

class FileDestination(
    private val file: File
) : CILoggingDestination() {

    override fun write(formattedMessage: CILoggingFormatter.FormattedMessage) {
        file.parentFile?.mkdirs()

        if (!file.exists()) {
            file.createNewFile()
        }

        file.appendText("${formattedMessage.toMessage()}${System.lineSeparator()}")
    }

    override fun child(tag: String): CILoggingDestination = FileDestination(
        file = File(
            file.parent,
            "${file.nameWithoutExtension}#$tag.${file.extension}"
        )
    )
}
