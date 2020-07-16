package com.avito.logger

interface Logger {
    fun debug(msg: String)
    fun warn(msg: String, error: Throwable? = null)
    fun exception(msg: String, error: Throwable)
    fun critical(msg: String, error: Throwable)
}
