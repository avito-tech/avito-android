package com.avito.logger

internal class GradleMetadataProvider(
    private val coordinates: GradleLoggerCoordinates
) : LoggerMetadataProvider {
    override fun provide(tag: String): LoggerMetadata {
        return GradleLoggerMetadata(tag, coordinates)
    }
}
