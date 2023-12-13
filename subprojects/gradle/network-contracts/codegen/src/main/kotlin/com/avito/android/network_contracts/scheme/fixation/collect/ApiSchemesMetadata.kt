package com.avito.android.network_contracts.scheme.fixation.collect

import kotlinx.serialization.Serializable

@Serializable
internal data class ApiSchemesMetadata(
    val projectName: String,
    val schemes: Map<String, String>,
)
