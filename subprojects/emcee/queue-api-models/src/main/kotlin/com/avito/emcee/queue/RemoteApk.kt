package com.avito.emcee.queue

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class RemoteApk(
    val location: ApkLocation,
    @Json(name = "package")
    val packageName: String,
)
