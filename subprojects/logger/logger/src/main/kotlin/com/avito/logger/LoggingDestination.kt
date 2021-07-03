package com.avito.logger

import java.io.Serializable

public interface LoggingDestination : Serializable {

    public fun write(
        level: LogLevel,
        message: String,
        throwable: Throwable?
    )
}
