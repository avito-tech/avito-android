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

    override fun exception(msg: String, error: Throwable) {
        ciLogger.critical(msg, error)
    }

    override fun critical(msg: String, error: Throwable) {
        ciLogger.critical(msg, error)
    }

    override fun warn(msg: String) {
        ciLogger.warn(msg)
    }
}
