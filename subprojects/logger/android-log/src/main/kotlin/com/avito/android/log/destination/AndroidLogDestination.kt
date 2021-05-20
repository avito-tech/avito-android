package com.avito.android.log.destination

import android.util.Log
import com.avito.android.log.AndroidTestMetadata
import com.avito.logger.LogLevel
import com.avito.logger.LoggingDestination

internal class AndroidLogDestination(private val metadata: AndroidTestMetadata) : LoggingDestination {

    override fun write(level: LogLevel, message: String, throwable: Throwable?) {
        when (level) {
            LogLevel.DEBUG -> Log.d(metadata.tag, message, throwable)
            LogLevel.INFO -> Log.i(metadata.tag, message, throwable)
            LogLevel.WARNING -> Log.w(metadata.tag, message, throwable)
            LogLevel.CRITICAL -> Log.e(metadata.tag, message, throwable)
        }
    }
}
