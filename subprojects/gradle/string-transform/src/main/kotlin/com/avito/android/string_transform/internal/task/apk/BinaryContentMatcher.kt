package com.avito.android.string_transform.internal.task.apk

internal fun ByteArray.containsSubsequence(needle: ByteArray): Boolean {
    if (needle.isEmpty()) return true
    if (needle.size > size) return false

    val lastStartIndex = size - needle.size
    for (startIndex in 0..lastStartIndex) {
        var matched = true
        for (offset in needle.indices) {
            if (this[startIndex + offset] != needle[offset]) {
                matched = false
                break
            }
        }
        if (matched) return true
    }

    return false
}
