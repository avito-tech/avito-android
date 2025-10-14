package com.avito.android.contracts.platform.scheme.validation.data

import com.avito.android.contracts.platform.scheme.collect.ApiSchemesMetadata

public fun interface ValidationApiSchemesService {

    public suspend fun validate(version: String, schemes: List<ApiSchemesMetadata>): List<RemoteValidationError>
}
