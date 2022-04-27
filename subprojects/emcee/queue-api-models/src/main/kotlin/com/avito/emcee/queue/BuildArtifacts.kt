package com.avito.emcee.queue

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class BuildArtifacts(
    @Json(name = "appApk")
    val app: ApkDescription,
    @Json(name = "testApk")
    val testApp: ApkDescription,
    val runnerClass: String,
)
