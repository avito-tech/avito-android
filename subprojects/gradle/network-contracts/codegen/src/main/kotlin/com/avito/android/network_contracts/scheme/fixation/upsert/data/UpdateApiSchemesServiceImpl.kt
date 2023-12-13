package com.avito.android.network_contracts.scheme.fixation.upsert.data

import com.avito.android.network_contracts.scheme.fixation.collect.ApiSchemesMetadata
import com.avito.android.network_contracts.scheme.fixation.upsert.data.models.UpdateApiSchemesRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import org.gradle.api.GradleException

internal class UpdateApiSchemesServiceImpl(
    private val httpClient: HttpClient,
) : UpdateApiSchemesService {

    override suspend fun sendContracts(
        author: String,
        version: String,
        schemes: List<ApiSchemesMetadata>
    ): Unit = supervisorScope {
        val projectSchemes = ApiSchemesMapper.mapSchemesToRequest(author, version, schemes)

        val updateProjectContractsJobs = projectSchemes
            .map { request -> async { sendProjectContracts(request) } }

        updateProjectContractsJobs.awaitAll()
    }

    private suspend fun sendProjectContracts(request: UpdateApiSchemesRequest) {
        val response = httpClient.sendContracts(request)
        if (response.status != HttpStatusCode.OK) {
            throw parseNetworkException(response)
        }
    }

    private fun parseNetworkException(response: HttpResponse): RuntimeException {
        return GradleException("Error while processing request:  <-- ${response.status} ${response.request.url}")
    }
}

private suspend fun HttpClient.sendContracts(
    request: UpdateApiSchemesRequest
): HttpResponse {
    return post {
        url(path = "service-api-composition-storage/upsertClientVersion/")
        contentType(ContentType.Application.Json)
        setBody(request)
    }
}
