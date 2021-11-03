package com.avito.logger.handler

import com.avito.logger.LogLevel

public interface LoggingHandler {

    public fun write(
        level: LogLevel,
        message: String,
        error: Throwable?
    )
}
