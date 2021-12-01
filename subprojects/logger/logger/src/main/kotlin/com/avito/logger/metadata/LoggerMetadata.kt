package com.avito.logger.metadata

public interface LoggerMetadata {
    public val asMessagePrefix: String
    public fun asMap(): Map<String, String>
}
