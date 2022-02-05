package com.avito.emcee.internal

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.io.File

internal class FilePathAdapter {

    @ToJson
    fun toJson(file: File): String {
        return file.path
    }

    @FromJson
    fun fromJson(filePath: String): File {
        return File(filePath)
    }
}
