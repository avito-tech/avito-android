package com.avito.emcee.queue

import com.squareup.moshi.Json

public data class TestEntry(
    val caseId: Int?,
    val tags: List<String>,
    @Json(name = "testName")
    val name: TestName
)
