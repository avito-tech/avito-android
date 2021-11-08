package com.avito.logger

import com.avito.logger.handler.LoggingHandler
import com.avito.logger.handler.LoggingHandlerProvider
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

internal class FileLoggingHandlerProvider(
    private val acceptedLogLevel: LogLevel,
    private val rootDir: File
) : LoggingHandlerProvider {

    override fun provide(metadata: LoggerMetadata): LoggingHandler {
        require(metadata is FileHandledLoggerMetadata) {
            "The metadata must be instanced of ${FileHandledLoggerMetadata::class.java} but was ${metadata::class.java}"
        }
        if (!rootDir.exists()) {
            Files.createDirectories(rootDir.toPath())
        }
        return FileLoggingHandler(acceptedLogLevel, Path.of(rootDir.absolutePath).relativize(metadata.logFilePath))
    }
}
