package com.avito.emcee.queue

import com.squareup.moshi.JsonClass
import kotlin.time.Duration

@JsonClass(generateAdapter = true)
public data class TestExecutionBehavior(
    val numberOfRetries: Int,
    val testMaximumDuration: Duration,
    val environment: Any = Any(),
    val logCapturingMode: String = "noLogs",
    val runnerWasteCleanupPolicy: String = "clean",
    val testRetryMode: String = "retryThroughQueue",
)
