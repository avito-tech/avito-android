package com.avito.logger.metadata

public interface LoggerMetadataProvider {
    public fun provide(tag: String): LoggerMetadata
}
