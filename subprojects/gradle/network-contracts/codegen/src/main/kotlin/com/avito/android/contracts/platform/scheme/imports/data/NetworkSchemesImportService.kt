package com.avito.android.contracts.platform.scheme.imports.data

import com.avito.android.contracts.platform.internal.http.HttpClientService
import com.avito.android.contracts.platform.scheme.imports.data.models.ApiSchemeImportResponse
import com.avito.android.contracts.platform.scheme.imports.data.models.ApiSchemesImportRequest
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
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

internal abstract class NetworkSchemesImportService : SchemesImportService<NetworkSchemesImportService.Parameters> {

    private val httpsClient by lazy { parameters.httpClient.get().get().buildClient() }

    override suspend fun importScheme(gateway: String, url: String): ApiSchemeImportResponse {
        return httpsClient.fetchSchema(gateway, url)
    }

    private suspend fun HttpClient.fetchSchema(gateway: String, url: String): ApiSchemeImportResponse {
        val response = fetchApiScheme(gateway, url)
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
    }
}

private suspend fun HttpClient.fetchApiScheme(
    gateway: String,
    apiPath: String
): HttpResponse = post {
    url(path = "service-api-composition-storage/getSchemaForPath/")
    contentType(ContentType.Application.Json)
    setBody(
        ApiSchemesImportRequest(
            path = apiPath,
            gatewayName = gateway.takeIf(String::isNotEmpty) ?: ApiSchemesImportRequest.DEFAULT_GATEWAY
        )
    )
}
