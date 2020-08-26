package com.avito.utils.logging

import java.io.File
import java.io.Serializable

interface CILoggingDestination : Serializable {

    fun write(message: String, throwable: Throwable?)
    fun child(tag: String): CILoggingDestination
}

internal object StdoutDestination : CILoggingDestination {

    override fun write(message: String, throwable: Throwable?) {
        println(message)
        throwable?.also { println(it) }
    }

    override fun child(tag: String): CILoggingDestination = this
}

internal object OnlyMessageStdoutDestination : CILoggingDestination {

    override fun write(message: String, throwable: Throwable?) {
        println(message)
        throwable?.message?.also { println(it) }
    }

    override fun child(tag: String): CILoggingDestination = this
}

internal class FileDestination(
    private val file: File
) : CILoggingDestination {

    override fun write(message: String, throwable: Throwable?) {
        file.parentFile?.mkdirs()

        if (!file.exists()) {
            file.createNewFile()
        }

        file.appendText("$message${System.lineSeparator()}")
        throwable?.also { file.appendText("$it${System.lineSeparator()}") }
    }

    override fun child(tag: String): CILoggingDestination = FileDestination(
        file = File(
            file.parent,
            "${file.nameWithoutExtension}#$tag.${file.extension}"
        )
    )
}
