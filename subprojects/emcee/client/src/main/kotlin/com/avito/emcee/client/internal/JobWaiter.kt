package com.avito.emcee.client.internal

import com.avito.emcee.queue.Job
import com.avito.emcee.queue.QueueApi
import com.avito.emcee.queue.status.JobStatusBody
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

internal class JobWaiter(
    private val queueApi: QueueApi
) {

    @ExperimentalTime
    suspend fun waitJobIsDone(job: Job, timeout: Duration) {
        withTimeout(timeout) {
            while (getTestsCount(job) > 0) {
                delay(5.seconds) // TODO how often? to config
            }
        }
    }

    private suspend fun getTestsCount(job: Job): Int {
        val status = queueApi.jobState(JobStatusBody(jobId = job.id)).jobState
        return status.queueState.runningQueueState.dequeuedBucketCount +
            status.queueState.runningQueueState.enqueuedBucketCount
    }
}
