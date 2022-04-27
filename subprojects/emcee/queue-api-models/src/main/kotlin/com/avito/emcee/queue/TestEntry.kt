package com.avito.emcee.queue

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class TestEntry(
    val caseId: Int?,
    val tags: List<String>,
    @Json(name = "testName")
    val name: TestName
)
