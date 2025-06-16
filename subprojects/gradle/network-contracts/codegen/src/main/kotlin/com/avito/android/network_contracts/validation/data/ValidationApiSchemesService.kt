package com.avito.android.network_contracts.validation.data

import com.avito.android.network_contracts.scheme.fixation.collect.ApiSchemesMetadata
import com.avito.android.network_contracts.validation.data.model.RemoteValidationError

internal fun interface ValidationApiSchemesService {

    suspend fun validate(version: String, schemes: List<ApiSchemesMetadata>): List<RemoteValidationError>
}
