package com.avito.instrumentation.internal

import com.avito.instrumentation.internal.TestRunResult.Verdict
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

internal object TestRunResultVerdictJsonSerializer : JsonSerializer<Verdict>, JsonDeserializer<Verdict> {

    override fun serialize(
        src: Verdict,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return when (src) {
            is Verdict.Success -> JsonObject().also { json ->
                json["type"] = "success"
                json["message"] = src.message
            }
            is Verdict.Failure -> JsonObject().also { json ->
                json["type"] = "failure"
                json["message"] = src.message
                json["prettifiedDetails"] = context.serialize(src.prettifiedDetails)
                json["cause"] = src.cause
            }
        }
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Verdict {
        return when (val type = json["type"].asString) {
            "success" -> Verdict.Success(
                message = json["message"].asString
            )
            "failure" -> Verdict.Failure(
                message = json["message"].asString,
                prettifiedDetails = context.deserialize(json["prettifiedDetails"], Verdict.Failure.Details::class.java),
                cause = null
            )
            else -> throw IllegalStateException("Incorrect type: $type")
        }
    }
}
