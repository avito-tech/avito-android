package com.avito.emcee.worker.internal

import com.avito.android.Problem
import com.avito.android.asRuntimeException
import com.avito.emcee.worker.GetBucketBody
import com.avito.emcee.worker.GetBucketResponse
import com.avito.emcee.worker.WorkerQueueApi
import com.avito.emcee.worker.configuration.PayloadSignature
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal class TestJobProducerImpl(
    private val api: WorkerQueueApi,
    private val workerId: String,
    private val payloadSignature: PayloadSignature,
) : TestJobProducer {

    override fun getJobs(): Flow<TestJobProducer.Job> = flow {
        while (currentCoroutineContext().isActive) {
            val response = api.getBucket(
                GetBucketBody(
                    workerId = workerId,
                    payloadSignature = payloadSignature,
                )
            ).getOrElse { throwable ->
                throw Problem(
                    shortDescription = "Connection to Emcee queue failed",
                    context = "Polling the Emcee queue for available buckets",
                    possibleSolutions = listOf("heck if Emcee queue is running and available"),
                    throwable = throwable
                ).asRuntimeException()
            }

            when (response) {
                is GetBucketResponse.Dequeued -> emit(
                    TestJobProducer.Job(
                        workerId = workerId,
                        payloadSignature = payloadSignature,
                        bucket = response.bucket
                    )
                )
                is GetBucketResponse.NoBucket -> delay(response.checkAfter)
            }
        }
    }
}
