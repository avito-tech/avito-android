package com.avito.logger

import com.avito.logger.metadata.LoggerMetadata
import com.avito.logger.metadata.LoggerMetadataProvider

internal class GradleMetadataProvider(
    private val coordinates: GradleLoggerCoordinates
) : LoggerMetadataProvider {
    override fun provide(tag: String): LoggerMetadata {
        return GradleLoggerMetadata(tag, coordinates)
    }
}
