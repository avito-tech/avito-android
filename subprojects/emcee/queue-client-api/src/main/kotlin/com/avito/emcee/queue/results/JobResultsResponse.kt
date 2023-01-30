package com.avito.emcee.queue.results

import com.avito.emcee.queue.BucketResultContainer
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class JobResultsResponse(
    val jobResults: JobResults
) {

    @JsonClass(generateAdapter = true)
    public data class JobResults(
        val jobId: String,
        val bucketResultContainers: List<BucketResultContainer>
    )
}
