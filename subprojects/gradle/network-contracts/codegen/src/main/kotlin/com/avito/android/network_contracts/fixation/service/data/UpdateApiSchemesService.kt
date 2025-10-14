package com.avito.android.network_contracts.fixation.service.data

import com.avito.android.contracts.platform.scheme.collect.ApiSchemesMetadata

internal interface UpdateApiSchemesService {

    suspend fun sendContracts(
        author: String,
        version: String,
        schemes: List<ApiSchemesMetadata>
    )
}
