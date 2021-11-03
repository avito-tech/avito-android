package com.avito.logger

public interface LoggerMetadata {
    public val tag: String
    public val logFileName: String
    public fun asString(): String

    public fun asMap(): Map<String, String>
}

internal class TagLoggerMetadata(override val tag: String) : LoggerMetadata {
    private val asString by lazy { "[$tag]" }
    override val logFileName: String = tag
    override fun asString() = asString
    override fun asMap(): Map<String, String> {
        return mapOf("tag" to tag)
    }
}

internal object TagLoggerMetadataProvider : LoggerMetadataProvider {

    override fun provide(tag: String) = TagLoggerMetadata(tag)
}

public interface LoggerMetadataProvider {
    public fun provide(tag: String): LoggerMetadata
}
