package com.avito.emcee.queue

import com.squareup.moshi.JsonClass

private typealias WorkerName = String

@JsonClass(generateAdapter = true)
public data class QueueState(
    val caseId: String,
    val runningQueueState: RunningQueueState, // TODO: use polymorphic adapter, queue could be in 'deleted' state
) {

    @JsonClass(generateAdapter = true)
    public data class RunningQueueState(
        val dequeuedBucketCount: Int,
        val dequeuedTests: List<Any>,
        val enqueuedBucketCount: Int,
        val enqueuedTests: List<TestName>,
    )
}
