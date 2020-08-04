package com.avito.utils.logging

import java.io.Serializable

/**
 * To obtain an instance use [ciLogger]
 *
 * Motivation for a custom logger:
 * - Serializable implementation to pass it through [org.gradle.workers.WorkerConfiguration.setParams]
 *
 * [#2678](https://github.com/gradle/gradle/issues/2678)
 */
open class CILogger(
    private val debugHandler: CILoggingHandler,
    private val infoHandler: CILoggingHandler,
    private val warnHandler: CILoggingHandler,
    private val criticalHandler: CILoggingHandler
) : Serializable {

    fun debug(message: String, error: Throwable? = null) {
        debugHandler.write(message, error)
    }

    fun info(message: String, error: Throwable? = null) {
        infoHandler.write(message, error)
    }

    fun warn(message: String, error: Throwable? = null) {
        warnHandler.write(message, error)
    }

    fun critical(message: String, error: Throwable? = null) {
        criticalHandler.write(message, error)
    }

    fun child(tag: String): CILogger = CILogger(
        debugHandler = debugHandler.child(tag),
        infoHandler = infoHandler.child(tag),
        warnHandler = warnHandler.child(tag),
        criticalHandler = criticalHandler.child(tag)
    )

    companion object {
        val allToStdout = CILogger(
            debugHandler = CILoggingHandlerImplementation(
                destination = StdoutDestination
            ),
            infoHandler = CILoggingHandlerImplementation(
                destination = StdoutDestination
            ),
            warnHandler = CILoggingHandlerImplementation(
                destination = StdoutDestination
            ),
            criticalHandler = CILoggingHandlerImplementation(
                destination = StdoutDestination
            )
        )
    }
}
