package com.avito.emcee.queue

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class Payload(
    val buildMetadata: BuildMetadata,
    @Json(name = "testDestination")
    val device: DeviceConfiguration,
    val testEntries: List<TestEntry>,
    val testExecutionBehavior: TestExecutionBehavior,
    val testTimeoutConfiguration: TestTimeoutConfiguration,
)
