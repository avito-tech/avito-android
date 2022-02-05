package com.avito.emcee.queue

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class TestExecutionBehavior(
    val environment: Map<String, String>,
    @Json(name = "numberOfRetries")
    val retries: Int
)
