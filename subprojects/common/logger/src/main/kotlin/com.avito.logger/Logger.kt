package com.avito.logger

interface Logger {
    fun debug(msg: String)
    fun warn(msg: String)
    fun exception(msg: String, error: Throwable)
    fun critical(msg: String, error: Throwable)
}
