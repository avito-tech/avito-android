package com.avito.android.log

import com.avito.logger.metadata.LoggerMetadata

internal class AndroidTestMetadata(
    private val tag: String,
    private val testName: String
) : LoggerMetadata {

    override val asMessagePrefix: String = "[$tag]"

    override fun asMap(): Map<String, String> {
        return mapOf(
            "tag" to tag,
            "test_name" to testName
        )
    }
}
