package com.avito.logger.handler

import android.util.Log
import com.avito.logger.LogLevel

internal class AndroidLogcatLoggingHandler(
    private val tag: String,
    acceptedLogLevel: LogLevel
) : LogLevelLoggingHandler(acceptedLogLevel) {

    override fun handleIfAcceptLogLevel(level: LogLevel, message: String, error: Throwable?) {
        when (level) {
            LogLevel.DEBUG -> Log.d(tag, message, error)
            LogLevel.INFO -> Log.i(tag, message, error)
            LogLevel.WARNING -> Log.w(tag, message, error)
            LogLevel.CRITICAL -> Log.e(tag, message, error)
        }
    }
}
