package com.avito.logger.metadata

internal class TagLoggerMetadata(private val tag: String) : LoggerMetadata {

    override val asMessagePrefix: String = "[$tag]"

    override fun asMap(): Map<String, String> {
        return mapOf("tag" to tag)
    }
}
