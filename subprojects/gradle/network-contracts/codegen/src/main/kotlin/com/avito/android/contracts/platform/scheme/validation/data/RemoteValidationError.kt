package com.avito.android.contracts.platform.scheme.validation.data

import kotlinx.serialization.Serializable

@Serializable
public data class RemoteValidationError(
    val message: String,
    val type: String,
)
