package com.avito.android.contracts.platform.scheme.collect

import kotlinx.serialization.Serializable

@Serializable
public data class ApiSchemesMetadata(
    val projectName: String,
    val schemes: Map<String, String>,
    val kind: String? = null,
)
