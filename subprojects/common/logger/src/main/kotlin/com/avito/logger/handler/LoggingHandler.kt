package com.avito.logger.handler

import com.avito.logger.LogLevel
import java.io.Serializable

interface LoggingHandler : Serializable {

    fun write(level: LogLevel, message: String, error: Throwable? = null)
}
