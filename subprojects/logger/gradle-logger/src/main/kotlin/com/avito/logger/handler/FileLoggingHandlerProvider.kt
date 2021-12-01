package com.avito.logger.handler

import com.avito.logger.FileHandledLoggerMetadata
import com.avito.logger.LogLevel
import com.avito.logger.metadata.LoggerMetadata
import java.io.File
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

internal class FileLoggingHandlerProvider(
    private val acceptedLogLevel: LogLevel,
    private val rootDir: File
) : LoggingHandlerProvider {

    override fun provide(metadata: LoggerMetadata): LoggingHandler {
        require(metadata is FileHandledLoggerMetadata) {
            "The metadata must be instanced of ${FileHandledLoggerMetadata::class.java} but was ${metadata::class.java}"
        }
        val rootDirPath = rootDir.toPath()
        if (!rootDirPath.exists()) rootDirPath.createDirectories()
        require(rootDirPath.isDirectory()) {
            "Must be a dir but was $rootDir"
        }

        val relativeLogFilePath = metadata.logFilePath
        val absoluteLogFilePath = rootDirPath.resolve(relativeLogFilePath)
        require(absoluteLogFilePath.isAbsolute) {
            "Must be absolute but was $absoluteLogFilePath"
        }
        val absoluteLogDirPath = absoluteLogFilePath.parent
        if (!absoluteLogDirPath.exists()) absoluteLogDirPath.createDirectories()
        require(absoluteLogDirPath.isDirectory()) {
            "Must be dir but was $absoluteLogDirPath"
        }
        return FileLoggingHandler(metadata.asMessagePrefix, acceptedLogLevel, absoluteLogFilePath)
    }
}
