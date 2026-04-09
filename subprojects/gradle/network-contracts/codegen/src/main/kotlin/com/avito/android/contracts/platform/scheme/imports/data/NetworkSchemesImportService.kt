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
import org.gradle.api.GradleException
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

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
        val additionalParams: MapProperty<String, String>
    }
}

private suspend fun HttpClient.fetchApiScheme(
    apiPath: String,
    additionalProperties: Map<String, String>
): HttpResponse = post {
    url(path = "getSchemaForPath/")
    contentType(ContentType.Application.Json)
    setBody(
        mapOf("path" to apiPath) + additionalProperties
    )
}
