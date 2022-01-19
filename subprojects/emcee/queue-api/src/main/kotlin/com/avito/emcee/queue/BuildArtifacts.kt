package com.avito.emcee.queue

import com.squareup.moshi.Json

public data class BuildArtifacts(
    @Json(name = "appApk")
    val appApkPath: String,
    @Json(name = "testApk")
    val testApkPath: String
)
