package com.avito.android.network_contracts.fixation.service.data

import com.avito.android.contracts.platform.scheme.collect.ApiSchemesMetadata
import com.avito.android.network_contracts.fixation.service.data.models.UpdateApiSchemesRequest
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
        val projectSchemes = ApiSchemesMapper.mapSchemesToRequest(schemes) { projectName, schemes ->
            UpdateApiSchemesRequest(
                author = author,
                appName = projectName,
                version = version,
                clientSchema = UpdateApiSchemesRequest.Schema(
                    schemes.toMap()
                ),
            )
        }

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
        url(path = "upsertClientVersion/")
        contentType(ContentType.Application.Json)
        setBody(request)
    }
}
