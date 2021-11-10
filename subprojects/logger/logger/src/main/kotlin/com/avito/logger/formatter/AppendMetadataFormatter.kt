package com.avito.logger.formatter

import com.avito.logger.LoggerMetadata
import com.avito.logger.LoggingFormatter

public object AppendMetadataFormatter : LoggingFormatter {

    override fun format(metadata: LoggerMetadata, message: String): String = buildString {
        append(metadata.asString())
        append(' ')
        append(message)
    }
}
