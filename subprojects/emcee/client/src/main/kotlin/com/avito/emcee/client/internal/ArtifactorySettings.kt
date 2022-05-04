package com.avito.emcee.client.internal

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class ArtifactorySettings(
    val baseUrl: String,
    val user: String,
    val password: String,
    val repository: String,
)
