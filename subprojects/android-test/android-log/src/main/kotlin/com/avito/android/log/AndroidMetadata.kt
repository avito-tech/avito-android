package com.avito.android.log

internal data class AndroidMetadata(val tag: String) {

    fun toMap(): Map<String, String> {
        return mapOf("tag" to tag)
    }
}
