package com.avito.emcee.queue

import com.squareup.moshi.JsonClass

private typealias WorkerName = String

@JsonClass(generateAdapter = true)
public data class QueueState(
    /**
     * something useless
     */
    val caseId: String,
    val enqueuedBucketCount: Int,
    val enqueuedTests: List<TestName>,
    val dequeuedBucketCount: Int,
    val dequeuedTests: Map<WorkerName, List<TestName>>,
)
