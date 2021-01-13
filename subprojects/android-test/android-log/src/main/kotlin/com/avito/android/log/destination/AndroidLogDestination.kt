package com.avito.android.log.destination

import android.util.Log
import com.avito.android.log.AndroidMetadata
import com.avito.logger.LogLevel
import com.avito.logger.LoggingDestination

internal class AndroidLogDestination(private val androidMetadata: AndroidMetadata) : LoggingDestination {

    override fun write(level: LogLevel, message: String, throwable: Throwable?) {
        when (level) {
            LogLevel.DEBUG -> Log.d(androidMetadata.tag, message, throwable)
            LogLevel.INFO -> Log.i(androidMetadata.tag, message, throwable)
            LogLevel.WARNING -> Log.w(androidMetadata.tag, message, throwable)
            LogLevel.CRITICAL -> Log.e(androidMetadata.tag, message, throwable)
        }
    }
}
