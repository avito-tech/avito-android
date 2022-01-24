package com.avito.emcee.queue

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
