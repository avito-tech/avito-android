package com.avito.android.string_transform.internal.task.apk

import java.io.File

internal fun interface TextFileDetector {
    fun isText(file: File): Boolean
}
