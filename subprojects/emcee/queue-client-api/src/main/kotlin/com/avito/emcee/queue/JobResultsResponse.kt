package com.avito.emcee.queue

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class JobResultsResponse(
    val jobResults: JobResults
) {

    @JsonClass(generateAdapter = true)
    public data class JobResults(
        @Json(name = "jobId")
        val id: String,
        val bucketResults: List<BucketResult>
    )
}
