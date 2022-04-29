package com.avito.emcee.queue

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class TestExecutionBehavior(
    val environment: Map<String, String>,
    @Json(name = "numberOfRetries")
    val retries: Int,
    /**
     * Should be deleted MBSA-556
     */
    val logCapturingMode: String = "noLogs",
    /**
     * Should be deleted MBSA-556
     */
    val runnerWasteCleanupPolicy: String = "clean",
    /**
     * Should be deleted MBSA-556
     */
    val testRetryMode: String = "retryThroughQueue",
    /**
     * Should be deleted MBSA-556
     */
    val userInsertedLibraries: List<String> = emptyList()
)
