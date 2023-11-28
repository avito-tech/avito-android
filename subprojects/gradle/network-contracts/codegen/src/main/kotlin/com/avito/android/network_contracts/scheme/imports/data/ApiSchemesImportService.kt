package com.avito.android.network_contracts.scheme.imports.data

import com.avito.android.network_contracts.scheme.imports.data.models.ApiSchemeImportResponse

internal interface ApiSchemesImportService {

    suspend fun importScheme(url: String): ApiSchemeImportResponse
}
