package com.avito.logger

interface Logger {
    fun debug(msg: String)
    // TODO: implement in clients and remove empty implementation
    fun info(msg: String) {} // empty implementation is for backward compatibility
    fun warn(msg: String, error: Throwable? = null)
    fun exception(msg: String, error: Throwable)
    fun critical(msg: String, error: Throwable)
}
