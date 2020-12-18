package com.avito.utils.logging

class RecordingLoggingHandler : CILoggingHandler {

    var lastMessage: String = ""
    val messages: MutableList<String> = mutableListOf()

    override fun write(message: String, error: Throwable?) {
        messages += message
        lastMessage = message
        println(message)
    }

    override fun child(tag: String): CILoggingHandler = this
}
