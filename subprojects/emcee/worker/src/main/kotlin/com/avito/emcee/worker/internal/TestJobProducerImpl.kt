package com.avito.emcee.worker.internal

import com.avito.emcee.worker.GetBucketBody
import com.avito.emcee.worker.GetBucketResponse
import com.avito.emcee.worker.RegisterWorkerBody
import com.avito.emcee.worker.WorkerQueueApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal class TestJobProducerImpl(
    private val api: WorkerQueueApi,
    private val workerId: String,
    private val restAddress: String
) : TestJobProducer {

    override fun getJobs(): Flow<TestJobProducer.Job> = flow {
        // TODO Handle an error
        val registerResponse = api.registerWorker(
            RegisterWorkerBody(
                workerId = workerId,
                workerRestAddress = restAddress
            )
        )

        // TODO Should we add waiting some external signals?
        while (true) {
            val bucket = api.getBucket(
                GetBucketBody(
                    workerId = workerId,
                    payloadSignature = registerResponse.payloadSignature,
                    workerCapabilities = emptyList() // TODO do we have to pass something?
                )
            )
            when (bucket) {
                is GetBucketResponse.Dequeued -> emit(TestJobProducer.Job(bucket))
                is GetBucketResponse.NoBucket -> delay(Duration.seconds(bucket.checkAfter))
            }
        }
    }
}
