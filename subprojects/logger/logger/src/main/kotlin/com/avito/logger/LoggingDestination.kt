package com.avito.logger

import java.io.Serializable

interface LoggingDestination : Serializable {

    fun write(level: LogLevel, message: String, throwable: Throwable?)
}
