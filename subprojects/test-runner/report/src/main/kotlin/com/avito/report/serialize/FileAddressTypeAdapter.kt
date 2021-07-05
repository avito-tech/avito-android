package com.avito.report.serialize

import com.avito.http.toHttpUrlResult
import com.avito.report.model.FileAddress
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

internal class FileAddressTypeAdapter : TypeAdapter<FileAddress>() {

    private val placeholderPrefix = "#upload:"

    private val errorPrefix = "#error:"

    override fun write(out: JsonWriter, value: FileAddress) {
        when (value) {
            is FileAddress.Error -> out.value("$errorPrefix${value.error.message}")
            is FileAddress.File -> out.value("$placeholderPrefix${value.fileName}")
            is FileAddress.URL -> out.value(value.url.toString())
        }
    }

    override fun read(`in`: JsonReader): FileAddress {
        val raw = `in`.nextString()
        return when {
            raw.startsWith(placeholderPrefix) ->
                FileAddress.File(fileName = raw.replace(placeholderPrefix, ""))

            raw.startsWith(errorPrefix) ->
                FileAddress.Error(error = IllegalStateException(raw.replace(errorPrefix, "")))

            else -> raw.toHttpUrlResult().fold(
                { FileAddress.URL(it) },
                { FileAddress.Error(it) }
            )
        }
    }
}
