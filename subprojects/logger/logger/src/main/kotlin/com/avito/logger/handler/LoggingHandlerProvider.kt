package com.avito.logger.handler

import com.avito.logger.metadata.LoggerMetadata

public interface LoggingHandlerProvider {
    public fun provide(metadata: LoggerMetadata): LoggingHandler
}
