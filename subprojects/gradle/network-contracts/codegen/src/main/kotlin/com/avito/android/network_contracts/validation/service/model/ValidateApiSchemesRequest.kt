package com.avito.android.network_contracts.validation.service.model

import kotlinx.serialization.Serializable

@Serializable
internal data class ValidateApiSchemesRequest(
    val appName: String,
    val clientSchema: Schema
) {

    @Serializable
    internal data class Schema(
        val schema: Map<String, String>
    )
}
