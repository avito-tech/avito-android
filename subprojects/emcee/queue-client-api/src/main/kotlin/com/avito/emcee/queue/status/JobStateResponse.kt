package com.avito.emcee.queue.status

import com.avito.emcee.queue.QueueState
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class JobStateResponse(
    val jobState: JobState,
) {

    @JsonClass(generateAdapter = true)
    public data class JobState(
        val jobId: String,
        val queueState: QueueState,
    )
}
