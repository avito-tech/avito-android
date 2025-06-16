package com.avito.android.network_contracts.validation.data.model

import kotlinx.serialization.Serializable

@Serializable
internal data class ValidateApiSchemesResultResponse(
    val result: ValidateApiSchemesResponse
)

@Serializable
internal data class ValidateApiSchemesResponse(
    val errors: List<RemoteValidationError>
)

@Serializable
internal data class RemoteValidationError(
    val message: String,
    val type: String,
)
