package com.avito.android.clickstream.api

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
public data class InfraClickStreamEventRequest(
    @SerialName("src_id") val srcId: Int,
    @SerialName("events") val events: List<InfraClickStreamEvent>,
)

@Serializable(with = InfraClickStreamEvent.Serializer::class)
public data class InfraClickStreamEvent(
    val eid: Int,
    val version: Int,
    val params: Map<String, String> = emptyMap(),
) {
    internal object Serializer : KSerializer<InfraClickStreamEvent> {

        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("InfraClickStreamEvent")

        override fun serialize(encoder: Encoder, value: InfraClickStreamEvent) {
            require(encoder is JsonEncoder) { "InfraClickStreamEvent supports JSON encoding only" }
            encoder.encodeJsonElement(
                buildJsonObject {
                    put("eid", value.eid)
                    put("version", value.version)
                    value.params.forEach { (key, paramValue) -> put(key, paramValue) }
                }
            )
        }

        override fun deserialize(decoder: Decoder): InfraClickStreamEvent =
            throw UnsupportedOperationException("Deserialization of InfraClickStreamEvent is not supported")
    }
}
