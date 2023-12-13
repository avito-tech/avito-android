package com.avito.android.network_contracts.scheme.fixation.upsert.data

import com.avito.android.network_contracts.scheme.fixation.collect.ApiSchemesMetadata

internal interface UpdateApiSchemesService {

    suspend fun sendContracts(
        author: String,
        version: String,
        schemes: List<ApiSchemesMetadata>
    )
}
