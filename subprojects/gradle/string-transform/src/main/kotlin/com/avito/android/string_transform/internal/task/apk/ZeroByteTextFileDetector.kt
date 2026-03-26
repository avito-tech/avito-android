package com.avito.android.string_transform.internal.task.apk

import java.io.File

internal class ZeroByteTextFileDetector(
    private val sampleSizeBytes: Int = 8_000,
) : TextFileDetector {

    override fun isText(file: File): Boolean {
        if (!file.isFile) return false
        if (file.length() == 0L) return true

        file.inputStream().use { input ->
            val bytes = ByteArray(sampleSizeBytes)
            val read = input.read(bytes)
            if (read <= 0) return true
            return bytes.take(read).none { it == 0.toByte() }
        }
    }
}
