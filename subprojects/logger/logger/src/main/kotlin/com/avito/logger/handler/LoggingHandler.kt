package com.avito.logger.handler

import com.avito.logger.LogLevel
import java.io.Serializable

public interface LoggingHandler : Serializable {

    public fun write(level: LogLevel, message: String, error: Throwable? = null)
}
