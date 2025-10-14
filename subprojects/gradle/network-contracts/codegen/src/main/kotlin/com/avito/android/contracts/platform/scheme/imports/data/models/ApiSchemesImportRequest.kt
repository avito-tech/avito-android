package com.avito.android.contracts.platform.scheme.imports.data.models

import kotlinx.serialization.Serializable

@Serializable
internal data class ApiSchemesImportRequest(
    val path: String,
    val gatewayName: String = DEFAULT_GATEWAY,
) {

    companion object {

        private const val DEFAULT_GATEWAY = "avito-api-gateway"
    }
}
