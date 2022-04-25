package com.avito.emcee.queue

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class TestTimeoutConfiguration(
    @Json(name = "singleTestMaximumDuration")
    public val testMaximumDurationSec: Long,
    @Json(name = "testRunnerMaximumSilenceDuration")
    public val runnerMaximumDurationSec: Long,
)
