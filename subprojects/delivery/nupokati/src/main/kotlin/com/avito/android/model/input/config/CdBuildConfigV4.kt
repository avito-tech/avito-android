package com.avito.android.model.input.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class CdBuildConfigV4(
    @SerialName("schema_version") override val schemaVersion: Long,
    @SerialName("project") override val project: String,
    @SerialName("release_version") override val releaseVersion: String,
    @SerialName("skip_upload") val skipUpload: Boolean,
) : CdBuildConfig
