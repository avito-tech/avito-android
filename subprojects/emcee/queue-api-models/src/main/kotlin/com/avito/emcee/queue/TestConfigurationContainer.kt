package com.avito.emcee.queue

import com.squareup.moshi.JsonClass
import kotlin.time.Duration

@JsonClass(generateAdapter = true)
public data class TestConfigurationContainer(
    val payload: Payload,
    val payloadType: String = "androidTestConfiguration",
) {
    @JsonClass(generateAdapter = true)
    public data class Payload(
        val androidBuildArtifacts: BuildArtifacts,
        val deviceType: String,
        val sdkVersion: Int,
        val testMaximumDuration: Duration,
    )
}
