package com.avito.emcee.queue

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class Payload(
    val testEntries: List<TestEntry>,
    val testConfiguration: TestConfiguration,
)
