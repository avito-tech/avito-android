package com.avito.android.model.output

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

internal class ArtifactsAdapter(schemaVersion: Long) {

    private val adapter: Json = Json {
        classDiscriminator = if (schemaVersion == 2L) {
            "artifact"
        } else {
            "type"
        }
    }

    fun toJson(artifacts: List<Artifact>): String = adapter.encodeToString(artifacts)

    fun fromJson(json: String): JsonElement = adapter.decodeFromString(json)
}
