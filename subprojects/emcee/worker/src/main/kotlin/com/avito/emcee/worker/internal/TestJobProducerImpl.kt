package com.avito.emcee.worker.internal

import com.avito.emcee.worker.GetBucketBody
import com.avito.emcee.worker.GetBucketResponse
import com.avito.emcee.worker.RegisterWorkerBody
import com.avito.emcee.worker.WorkerQueueApi
import com.avito.emcee.worker.internal.networking.SocketAddress
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal class TestJobProducerImpl(
    private val api: WorkerQueueApi,
    private val workerId: String,
    private val workerAddress: SocketAddress
) : TestJobProducer {

    override fun getJobs(): Flow<TestJobProducer.Job> = flow {
        // TODO Handle an error
        val registerResponse = api.registerWorker(
            RegisterWorkerBody(
                workerId = workerId,
                workerRestAddress = workerAddress.serialized()
            )
        )

        // TODO Should we add waiting some external signals?
        while (true) {
            val response = api.getBucket(
                GetBucketBody(
                    workerId = workerId,
                    payloadSignature = registerResponse.workerConfiguration.payloadSignature,
                    workerCapabilities = emptyList() // TODO do we have to pass something?
                )
            )
            when (response) {
                is GetBucketResponse.Dequeued -> emit(TestJobProducer.Job(response.bucket))
                is GetBucketResponse.NoBucket -> delay(Duration.seconds(response.checkAfter))
            }
        }
    }
}
