package com.avito.logger

import com.avito.logger.metadata.LoggerMetadata
import java.nio.file.Path

/**
 * Special type of the [LoggerMetadata] that will be handled by [FileLoggingHandler]
 */
internal interface FileHandledLoggerMetadata : LoggerMetadata {
    val logFilePath: Path
}
