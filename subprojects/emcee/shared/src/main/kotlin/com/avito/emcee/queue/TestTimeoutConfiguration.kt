package com.avito.emcee.queue

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class TestTimeoutConfiguration(
    /**
     * Expected in seconds
     */
    val singleTestMaximumDuration: Float,
    /**
     * Expected in seconds
     */
    val testRunnerMaximumSilenceDuration: Float
)
