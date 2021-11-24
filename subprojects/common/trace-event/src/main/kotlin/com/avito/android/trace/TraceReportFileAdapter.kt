package com.avito.android.trace

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.io.File
import java.lang.reflect.Type

public class TraceReportFileAdapter(
    private val file: File
) {

    private val gson: Gson by lazy {
        GsonBuilder()
            .registerTypeAdapter(TraceEvent::class.java, TraceEventSerializer())
            .registerTypeAdapter(TraceEvent::class.java, TraceEventDeserializer())
            .create()
    }

    public fun write(report: TraceReport) {
        file.writeText(gson.toJson(report))
    }

    public fun read(): TraceReport {
        return gson.fromJson(file.bufferedReader(), TraceReport::class.java)
    }

    private class TraceEventDeserializer : JsonDeserializer<TraceEvent> {

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): TraceEvent {
            val jsonObject = json.asJsonObject

            return when (val phase = jsonObject.get("ph").asCharacter) {
                DurationEvent.PHASE_BEGIN, DurationEvent.PHASE_END -> context.deserialize<DurationEvent>(
                    json,
                    DurationEvent::class.java
                )
                CompleteEvent.PHASE -> context.deserialize<CompleteEvent>(json, CompleteEvent::class.java)
                InstantEvent.PHASE -> context.deserialize<InstantEvent>(json, InstantEvent::class.java)
                else -> throw IllegalArgumentException("Unsupported event type: $phase")
            }
        }
    }

    private class TraceEventSerializer : JsonSerializer<TraceEvent> {

        override fun serialize(src: TraceEvent, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return when (src) {
                is DurationEvent -> context.serialize(src, DurationEvent::class.java)
                is CompleteEvent -> context.serialize(src, CompleteEvent::class.java)
                is InstantEvent -> context.serialize(src, InstantEvent::class.java)
                else -> throw IllegalArgumentException("Unsupported event type: ${src.javaClass}")
            }
        }
    }
}
