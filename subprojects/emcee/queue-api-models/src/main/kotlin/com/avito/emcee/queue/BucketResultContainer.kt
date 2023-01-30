package com.avito.emcee.queue

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class BucketResultContainer(
    val payload: BucketResult,
    val payloadType: String = "testingResult"
)
