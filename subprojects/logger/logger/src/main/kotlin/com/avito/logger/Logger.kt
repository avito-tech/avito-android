package com.avito.logger

/**
 * discussion about log levels https://github.com/avito-tech/avito-android/discussions/698
 */
public interface Logger {

    /**
     * additional information for troubleshooting
     */
    public fun debug(msg: String)

    /**
     * to understand system state
     */
    public fun info(msg: String)

    /**
     * unwanted state but can proceed
     */
    public fun warn(msg: String, error: Throwable? = null)

    /**
     * could not recover from this state
     */
    public fun critical(msg: String, error: Throwable)
}
