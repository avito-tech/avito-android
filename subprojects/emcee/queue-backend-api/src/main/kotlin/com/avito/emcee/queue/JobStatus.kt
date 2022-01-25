package com.avito.emcee.queue

import com.squareup.moshi.Json

public data class JobStatus(
    @Json(name = "jobId")
    val id: String,
    val queueState: QueueState,
)
