package com.avito.android.model.input

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class OutputDescriptor(
    val path: String,
    @SerialName("skip_upload") val skipUpload: Boolean
)
