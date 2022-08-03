package com.avito.emcee.client.internal.result

import com.avito.emcee.queue.BucketResult
import com.avito.emcee.queue.Job
import com.avito.emcee.queue.JobStatusBody
import com.avito.emcee.queue.QueueApi

internal class JobResultResolver(
    private val queueApi: QueueApi
) {

    suspend fun resolveResult(job: Job): JobResult {

        val unfilteredResults: List<BucketResult.UnfilteredResult> = queueApi.jobResults(JobStatusBody(job.id))
            .jobResults
            .bucketResults
            .map { it.unfilteredResults }
            .flatten()

        // TODO: compare successful tests with scheduled
        val (_, failedTests) = unfilteredResults.partition { result: BucketResult.UnfilteredResult ->
            result.testRunResults.map { it.succeeded }.contains(true)
        }

        return if (failedTests.isEmpty()) {
            JobResult.Success
        } else {
            JobResult.Failure(failedTests.map { it.testEntry })
        }
    }
}
