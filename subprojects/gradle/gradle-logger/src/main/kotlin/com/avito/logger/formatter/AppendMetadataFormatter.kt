package com.avito.logger.formatter

import com.avito.logger.LoggerMetadata
import com.avito.logger.LoggingFormatter

internal class AppendMetadataFormatter(private val metadata: LoggerMetadata) : LoggingFormatter {

    override fun format(message: String): String = buildString {
        append('[')
        append(metadata.tag)
        if (!metadata.pluginName.isNullOrBlank()) {
            append('|')
            append(metadata.pluginName)
        }
        if (!metadata.projectPath.isNullOrBlank() || !metadata.taskName.isNullOrBlank()) {
            append('@')
        }
        if (!metadata.projectPath.isNullOrBlank()) {
            append(metadata.projectPath)
        }
        if (!metadata.taskName.isNullOrBlank()) {
            append(':')
            append(metadata.taskName)
        }
        append(']')
        append(' ')
        append(message)
    }
}
