package com.avito.emcee.queue.status

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class JobStatusBody(
    val jobId: String
)
