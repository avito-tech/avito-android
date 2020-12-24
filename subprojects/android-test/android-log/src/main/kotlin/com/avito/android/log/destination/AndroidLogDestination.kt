package com.avito.android.log.destination

import android.util.Log
import com.avito.logger.LogLevel
import com.avito.logger.LoggingDestination

internal class AndroidLogDestination(private val tag: String) : LoggingDestination {

    override fun write(level: LogLevel, message: String, throwable: Throwable?) {
        when (level) {
            LogLevel.DEBUG -> Log.d(tag, message, throwable)
            LogLevel.INFO -> Log.i(tag, message, throwable)
            LogLevel.WARNING -> Log.w(tag, message, throwable)
            LogLevel.CRITICAL -> Log.e(tag, message, throwable)
        }
    }
}
