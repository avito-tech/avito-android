package com.avito.android.network_contracts.scheme.fixation.upsert.data.models

import kotlinx.serialization.Serializable

@Serializable
internal data class UpdateApiSchemesRequest(
    val author: String,
    val appName: String,
    val version: String,
    val clientSchema: Schema
) {

    @Serializable
    internal data class Schema(
        val schema: Map<String, String>
    )
}
