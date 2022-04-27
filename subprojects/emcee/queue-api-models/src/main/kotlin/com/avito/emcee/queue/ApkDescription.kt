package com.avito.emcee.queue

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class ApkDescription(
    val location: ApkLocation,
    @Json(name = "package")
    val apkPackage: String,
)
