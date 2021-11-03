package com.avito.android.log

import com.avito.logger.LoggerMetadata

internal data class AndroidTestMetadata(
    override val tag: String,
    val testName: String
) : LoggerMetadata {

    override val logFileName: String = testName
    override fun asString(): String = tag
    override fun asMap(): Map<String, String> {
        return mapOf(
            "tag" to tag,
            "test_name" to testName
        )
    }
}
