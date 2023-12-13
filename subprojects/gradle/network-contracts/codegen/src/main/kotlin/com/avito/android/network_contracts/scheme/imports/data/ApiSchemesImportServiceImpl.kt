package com.avito.android.network_contracts.scheme.imports.data

import com.avito.android.network_contracts.scheme.imports.data.models.ApiSchemeImportResponse
import com.avito.android.network_contracts.scheme.imports.data.models.ApiSchemesImportRequest
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

internal class ApiSchemesImportServiceImpl(
    private val httpClient: HttpClient,
) : ApiSchemesImportService {

    override suspend fun importScheme(url: String): ApiSchemeImportResponse {
        return httpClient.fetchSchema(url)
    }

    private suspend fun HttpClient.fetchSchema(url: String): ApiSchemeImportResponse {
        val response = fetchApiScheme(url)
        if (response.status == HttpStatusCode.OK) {
            return response.body()
        } else {
            throw parseNetworkException(response)
        }
    }

    private fun parseNetworkException(response: HttpResponse): RuntimeException {
        return GradleException("Error while processing request:  <-- ${response.status} ${response.request.url}")
    }
}

private suspend fun HttpClient.fetchApiScheme(
    apiPath: String
): HttpResponse = post {
    url(path = "service-api-composition-storage/getSchemaForPath/")
    contentType(ContentType.Application.Json)
    setBody(ApiSchemesImportRequest(path = apiPath))
}
