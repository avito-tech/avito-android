package com.avito.logger.metadata

internal object TagLoggerMetadataProvider : LoggerMetadataProvider {

    override fun provide(tag: String) = TagLoggerMetadata(tag)
}
