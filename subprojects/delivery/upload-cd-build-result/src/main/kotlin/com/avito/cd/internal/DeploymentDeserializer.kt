package com.avito.cd.internal

import com.avito.cd.CdBuildConfig
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

internal class DeploymentDeserializer : JsonDeserializer<CdBuildConfig.Deployment> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): CdBuildConfig.Deployment {
        return when (val type = json.asJsonObject.get("type").asString) {
            "google-play" ->
                context.deserialize<CdBuildConfig.Deployment.GooglePlay>(
                    json,
                    CdBuildConfig.Deployment.GooglePlay::class.java
                )
            "ru-store" ->
                context.deserialize<CdBuildConfig.Deployment.RuStore>(
                    json,
                    CdBuildConfig.Deployment.RuStore::class.java
                )
            "qapps" ->
                context.deserialize<CdBuildConfig.Deployment.Qapps>(
                    json,
                    CdBuildConfig.Deployment.Qapps::class.java
                )
            else -> CdBuildConfig.Deployment.Unknown(type)
        }
    }
}
