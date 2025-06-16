package com.avito.android.network_contracts.validation.data.model

import kotlinx.serialization.Serializable

@Serializable
internal data class ValidateApiSchemesRequest(
    val appName: String,
    val version: String,
    val clientSchema: Schema
) {

    @Serializable
    internal data class Schema(
        val schema: Map<String, String>
    )
}
