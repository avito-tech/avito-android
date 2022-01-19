package com.avito.emcee.queue

private typealias WorkerName = String

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
