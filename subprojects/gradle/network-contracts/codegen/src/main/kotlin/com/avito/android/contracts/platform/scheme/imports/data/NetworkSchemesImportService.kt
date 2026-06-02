package com.avito.android.contracts.platform.scheme.imports.data

import com.avito.android.contracts.platform.internal.http.HttpClientService
import com.avito.android.contracts.platform.scheme.imports.data.models.ApiSchemeImportResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.gradle.api.GradleException
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import java.io.Serializable

internal abstract class NetworkSchemesImportService : SchemesImportService<NetworkSchemesImportService.Parameters> {

    private val httpsClient by lazy { parameters.httpClient.get().get().buildClient() }

    override suspend fun importScheme(url: String): ApiSchemeImportResponse {
        return httpsClient.fetchSchema(url)
    }

    private suspend fun HttpClient.fetchSchema(url: String): ApiSchemeImportResponse {
        val additionalProperties = parameters.additionalParams.get()
        val response = fetchApiScheme(url, additionalProperties)
        if (response.status == HttpStatusCode.OK) {
            return response.body()
        } else {
            throw parseNetworkException(response)
        }
    }

    private fun parseNetworkException(response: HttpResponse): RuntimeException {
        return GradleException("Error while processing request:  <-- ${response.status} ${response.request.url}")
    }

    interface Parameters : SchemesImportService.Parameters {

        val httpClient: Property<Provider<HttpClientService>>
        val additionalParams: MapProperty<String, ParamType>
    }
}

private suspend fun HttpClient.fetchApiScheme(
    apiPath: String,
    additionalProperties: Map<String, ParamType>,
): HttpResponse = post {
    url(path = "getSchemaForPath/")
    contentType(ContentType.Application.Json)
    setBody(
        buildJsonObject {
            put("path", JsonPrimitive(apiPath))
            additionalProperties.forEach { (key, value) ->
                put(key, value.asJsonElement())
            }
        }
    )
}

private fun ParamType.asJsonElement(): JsonElement {
    return when (this) {
        is ParamType.Primitive -> when (value) {
            is String -> JsonPrimitive(value)
            is Number -> JsonPrimitive(value)
            is Boolean -> JsonPrimitive(value)
            else -> throw IllegalArgumentException("Unsupported primitive type")
        }

        is ParamType.ListType -> JsonArray(value.map { it.asJsonElement() })
        is ParamType.MapType -> JsonObject(value.mapValues { it.value.asJsonElement() })
    }
}

public sealed interface ParamType : Serializable {

    public data class Primitive(val value: Serializable?) : ParamType
    public data class ListType(val value: List<ParamType>) : ParamType
    public data class MapType(val value: Map<String, ParamType>) : ParamType
}
