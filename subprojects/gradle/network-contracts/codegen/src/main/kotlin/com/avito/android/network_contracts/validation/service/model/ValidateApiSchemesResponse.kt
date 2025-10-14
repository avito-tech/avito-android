package com.avito.android.network_contracts.validation.service.model

import com.avito.android.contracts.platform.scheme.validation.data.RemoteValidationError
import kotlinx.serialization.Serializable

@Serializable
internal data class ValidateApiSchemesResultResponse(
    val result: ValidateApiSchemesResponse
)

@Serializable
internal data class ValidateApiSchemesResponse(
    val errors: List<RemoteValidationError> = emptyList()
)
