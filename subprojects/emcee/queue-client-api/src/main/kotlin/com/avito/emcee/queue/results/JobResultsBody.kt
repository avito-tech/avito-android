package com.avito.emcee.queue.results

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class JobResultsBody(
    val jobId: String
)
