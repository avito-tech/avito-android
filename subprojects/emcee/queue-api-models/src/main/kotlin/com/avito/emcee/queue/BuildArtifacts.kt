package com.avito.emcee.queue

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class BuildArtifacts(
    @Json(name = "appApk")
    val app: RemoteApk,
    @Json(name = "testApk")
    val testApp: RemoteApk,
    val runnerClass: String,
)
