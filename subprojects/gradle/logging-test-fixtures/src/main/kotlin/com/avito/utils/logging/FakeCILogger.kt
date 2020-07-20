package com.avito.utils.logging

@Suppress("MemberVisibilityCanBePrivate", "CanBeParameter")
class FakeCILogger(
    val debugHandler: RecordingLoggingHandler = RecordingLoggingHandler(),
    val infoHandler: RecordingLoggingHandler = RecordingLoggingHandler(),
    val warnHandler: RecordingLoggingHandler = RecordingLoggingHandler(),
    val criticalHandler: RecordingLoggingHandler = RecordingLoggingHandler()
) : com.avito.utils.logging.CILogger(
    debugHandler = debugHandler,
    infoHandler = infoHandler,
    warnHandler = warnHandler,
    criticalHandler = criticalHandler
)

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
