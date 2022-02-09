package com.avito.emcee.queue

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class QueueVersion(
    val caseId: String,
    val version: String
)
