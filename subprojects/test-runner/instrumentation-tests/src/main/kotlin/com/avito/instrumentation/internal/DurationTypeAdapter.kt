package com.avito.instrumentation.internal

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.Duration

internal class DurationTypeAdapter : TypeAdapter<Duration>() {

    override fun write(writer: JsonWriter, duration: Duration) {
        writer.value(duration.toNanos())
    }

    override fun read(reader: JsonReader): Duration {
        return Duration.ofNanos(reader.nextLong())
    }
}
