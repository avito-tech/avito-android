package com.avito.emcee.queue

import com.squareup.moshi.JsonClass
import kotlin.time.Duration

@JsonClass(generateAdapter = true)
public data class TestConfiguration(
    val buildArtifacts: BuildArtifacts,
    val deviceType: String,
    val sdkVersion: Int,
    val testExecutionBehavior: TestExecutionBehavior,
    val testMaximumDuration: Duration,
)
