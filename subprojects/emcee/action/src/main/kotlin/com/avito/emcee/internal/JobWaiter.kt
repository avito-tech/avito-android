package com.avito.emcee.internal

import com.avito.emcee.queue.Job
import com.avito.emcee.queue.JobStatusBody
import com.avito.emcee.queue.QueueApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

internal class JobWaiter(
    private val queueApi: QueueApi
) {

    @ExperimentalTime
    suspend fun wait(job: Job, timeout: Duration) {
        withTimeout(timeout) {
            do {
                // TODO handle error
                val status = queueApi.jobStatus(JobStatusBody(id = job.id))
                delay(Duration.seconds(10)) // TODO how often? to config
            } while (status.queueState.dequeuedBucketCount + status.queueState.enqueuedBucketCount != 0)
        }
    }
}
