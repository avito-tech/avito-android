package com.avito.android.log

import com.avito.logger.LoggerMetadata
import com.avito.logger.LoggerMetadataProvider

class AndroidTestLoggerMetadataProvider(
    private val testName: String
) : LoggerMetadataProvider {
    override fun provide(tag: String): LoggerMetadata {
        return AndroidTestMetadata(tag, testName)
    }
}
