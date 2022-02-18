package com.avito.emcee.worker.internal

import com.avito.emcee.worker.GetBucketResponse
import kotlinx.coroutines.flow.Flow

internal interface TestJobProducer {

    data class Job(
        val bucket: GetBucketResponse.Dequeued
    )

    fun getJobs(): Flow<Job>
}
