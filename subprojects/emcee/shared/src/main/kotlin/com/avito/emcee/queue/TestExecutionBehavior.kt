package com.avito.emcee.queue

import com.squareup.moshi.Json

public data class TestExecutionBehavior(
    val environment: Map<String, String>,
    @Json(name = "numberOfRetries")
    val retries: Int
)
