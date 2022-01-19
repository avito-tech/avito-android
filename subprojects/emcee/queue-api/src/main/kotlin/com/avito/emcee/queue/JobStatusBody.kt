package com.avito.emcee.queue

import com.squareup.moshi.Json

public data class JobStatusBody(
    @Json(name = "jobId")
    val id: String
)
