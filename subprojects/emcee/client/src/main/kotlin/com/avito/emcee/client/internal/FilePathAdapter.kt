package com.avito.emcee.client.internal

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import java.io.File

internal class FilePathAdapter : JsonAdapter<File>() {

    override fun toJson(writer: JsonWriter, value: File?) {
        when (value) {
            null -> writer.nullValue()
            else -> writer.value(value.path)
        }
    }

    override fun fromJson(reader: JsonReader): File {
        return File(reader.nextString())
    }
}
