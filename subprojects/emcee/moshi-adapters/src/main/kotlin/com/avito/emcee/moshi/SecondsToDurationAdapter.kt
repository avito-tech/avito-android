package com.avito.emcee.moshi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * In the Apple world (and Emcee queue) seconds are used for time measurement by default.
 */
public class SecondsToDurationAdapter : JsonAdapter<Duration>() {

    override fun toJson(writer: JsonWriter, value: Duration?) {
        when (value) {
            null -> writer.nullValue()
            else -> writer.value(value.inWholeSeconds)
        }
    }

    override fun fromJson(reader: JsonReader): Duration {
        return reader.nextLong().seconds
    }
}
