package com.avito.emcee.discoverer.gson

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

internal class HttpUrlAdapter : TypeAdapter<HttpUrl>() {

    override fun write(output: JsonWriter, value: HttpUrl) {
        output.value(value.toString())
    }

    override fun read(input: JsonReader): HttpUrl {
        return input.nextString().toHttpUrl()
    }
}
