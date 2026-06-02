package com.avito.android.network_contracts.validation.service

import com.avito.android.contracts.platform.scheme.collect.ApiSchemesMetadata
import com.avito.android.contracts.platform.scheme.validation.data.RemoteValidationError
import com.avito.android.contracts.platform.scheme.validation.data.ValidationApiSchemesService
import com.avito.android.network_contracts.fixation.service.data.ApiSchemesMapper
import com.avito.android.network_contracts.validation.service.model.ValidateApiSchemesRequest
import com.avito.android.network_contracts.validation.service.model.ValidateApiSchemesResultResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import org.gradle.api.GradleException

internal class ValidationApiSchemesServiceImpl(
    private val httpClient: HttpClient,
) : ValidationApiSchemesService {

    override suspend fun validate(
        branch: String,
        schemes: List<ApiSchemesMetadata>,
        ): List<RemoteValidationError> = supervisorScope {
        val requests = ApiSchemesMapper.mapSchemesToRequest(schemes) { projectName, projectSchemes ->
            ValidateApiSchemesRequest(
                appName = projectName,
                branch = branch,
                clientSchema = ValidateApiSchemesRequest.Schema(projectSchemes.toMap())
            )
        }

        requests
            .map { request -> async { sendContracts(request) } }
            .awaitAll()
            .flatten()
            .distinctBy { it.message }
    }

    private suspend fun sendContracts(request: ValidateApiSchemesRequest): List<RemoteValidationError> {
        val response = httpClient.validateSchemes(request)
        if (response.status != HttpStatusCode.OK) {
            throw parseNetworkException(response)
        }
        val result = response.body<ValidateApiSchemesResultResponse>().result
        return result.errors
    }

    private suspend fun parseNetworkException(response: HttpResponse): RuntimeException {
        val details = "${response.status} ${response.request.url} ${response.bodyAsText()}"
        return GradleException("Error while processing request:  <-- $details")
    }

    private suspend fun HttpClient.validateSchemes(
        request: ValidateApiSchemesRequest
    ): HttpResponse {
        return post {
            url("validateSchema/")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}
