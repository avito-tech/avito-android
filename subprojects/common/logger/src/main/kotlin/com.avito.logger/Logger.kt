package com.avito.logger

interface Logger {
    fun debug(msg: String)
    fun info(msg: String)
    fun warn(msg: String, error: Throwable? = null)
    fun critical(msg: String, error: Throwable) {} // empty implementation is for backward compatibility
}
