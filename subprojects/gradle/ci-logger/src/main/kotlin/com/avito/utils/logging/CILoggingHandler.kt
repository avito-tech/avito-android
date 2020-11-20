package com.avito.utils.logging

import java.io.Serializable

interface CILoggingHandler : Serializable {
    fun write(message: String, error: Throwable? = null)
    fun child(tag: String): CILoggingHandler
}

internal object NothingLoggingHandler : CILoggingHandler {
    override fun write(message: String, error: Throwable?) {}
    override fun child(tag: String): CILoggingHandler = this
}

internal class CILoggingHandlerImplementation(
    private val formatter: CILoggingFormatter? = null,
    private val destination: CILoggingDestination
) : CILoggingHandler {

    override fun write(message: String, error: Throwable?) {
        destination.write(
            formatter?.format(message) ?: message,
            error
        )
    }

    override fun child(tag: String): CILoggingHandler = CILoggingHandlerImplementation(
        formatter = formatter?.child(tag),
        destination = destination.child(tag)
    )
}

internal class CILoggingCombinedHandler(
    private val handlers: Collection<CILoggingHandler>
) : CILoggingHandler {

    override fun write(message: String, error: Throwable?) {
        handlers.forEach { it.write(message, error) }
    }

    override fun child(tag: String): CILoggingHandler = CILoggingCombinedHandler(
        handlers = handlers.map { it.child(tag) }
    )
}
