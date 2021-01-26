package com.avito.android.log

internal data class AndroidTestMetadata(
    val tag: String,
    val testName: String?
) {

    fun toMap(): Map<String, String> {
        val result = mutableMapOf("tag" to tag)

        if (!testName.isNullOrBlank()) {
            result["test_name"] = testName
        }

        return result
    }
}
