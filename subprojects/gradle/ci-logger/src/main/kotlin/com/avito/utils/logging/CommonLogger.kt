package com.avito.utils.logging

import com.avito.logger.Logger

fun commonLogger(ciLogger: CILogger): Logger {
    return CILoggerWrapper(ciLogger)
}

private class CILoggerWrapper(
    private val ciLogger: CILogger
) : Logger {

    override fun debug(msg: String) {
        ciLogger.debug(msg)
    }

    override fun info(msg: String) {
        ciLogger.info(msg)
    }

    override fun critical(msg: String, error: Throwable) {
        ciLogger.critical(msg, error)
    }

    override fun warn(msg: String, error: Throwable?) {
        ciLogger.warn(msg, error)
    }
}
