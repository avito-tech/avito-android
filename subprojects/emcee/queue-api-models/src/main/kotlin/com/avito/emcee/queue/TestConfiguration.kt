package com.avito.emcee.queue

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class TestConfiguration(
    val buildArtifacts: BuildArtifacts,
    val deviceType: String,
    val sdkVersion: Int,
    val testExecutionBehavior: TestExecutionBehavior,
    @Json(name = "testMaximumDuration")
    val testMaximumDurationSec: Long,
)
