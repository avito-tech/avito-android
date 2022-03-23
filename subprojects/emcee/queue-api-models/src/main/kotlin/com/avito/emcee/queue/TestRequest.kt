package com.avito.emcee.queue

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class TestRequest(
    val buildArtifacts: BuildArtifacts,
    @Json(name = "testDestination")
    val device: DeviceConfiguration,
    val testEntry: TestEntry,
    @Json(name = "testExecutionBehavior")
    val executionBehavior: TestExecutionBehavior,
    @Json(name = "testMaximumDuration")
    val testMaximumDurationSec: Long,
    /**
     * Some analytics from queue about job
     * Must be empty object to skip
     */
    val analyticsConfiguration: Any,
)
