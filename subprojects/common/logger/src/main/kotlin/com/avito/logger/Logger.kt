package com.avito.logger

/**
 * discussion about log levels https://github.com/avito-tech/avito-android/discussions/698
 */
interface Logger {

    /**
     * additional information for troubleshooting
     */
    fun debug(msg: String)

    /**
     * to understand system state
     */
    fun info(msg: String)

    /**
     * unwanted state but can proceed
     */
    fun warn(msg: String, error: Throwable? = null)

    /**
     * could not recover from this state
     */
    fun critical(msg: String, error: Throwable)
}
