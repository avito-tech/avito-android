package com.avito.logger

interface Logger {
    fun debug(msg: String)
    fun info(msg: String)
    fun warn(msg: String, error: Throwable? = null)
    fun critical(msg: String, error: Throwable) {} // empty implementation is for backward compatibility

    // TODO remove in MBS-9283
    @Deprecated("Never called. Implement critical instead.")
    fun exception(msg: String, error: Throwable) {} // empty implementation is for backward compatibility
}
