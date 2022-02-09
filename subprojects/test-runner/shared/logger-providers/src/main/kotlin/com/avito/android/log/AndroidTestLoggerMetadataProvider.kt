package com.avito.android.log

import com.avito.logger.metadata.LoggerMetadata
import com.avito.logger.metadata.LoggerMetadataProvider

public class AndroidTestLoggerMetadataProvider(
    private val testName: String
) : LoggerMetadataProvider {
    override fun provide(tag: String): LoggerMetadata {
        return AndroidTestMetadata(tag, testName)
    }
}
