package com.avito.emcee.worker.internal

import com.avito.android.Problem
import com.avito.android.asRuntimeException
import com.avito.emcee.worker.GetBucketBody
import com.avito.emcee.worker.GetBucketResponse
import com.avito.emcee.worker.RegisterWorkerBody
import com.avito.emcee.worker.WorkerQueueApi
import com.avito.emcee.worker.internal.networking.SocketAddress
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
    private val workerAddress: SocketAddress
) : TestJobProducer {

    override fun getJobs(): Flow<TestJobProducer.Job> = flow {
        val registerResponse = api.registerWorker(
            RegisterWorkerBody(
                workerId = workerId,
                workerRestAddress = workerAddress.serialized()
            )
        ).getOrElse { throwable ->
            throw throwable
                .toQueueProblem("Registering a new worker at startup")
                .asRuntimeException()
        }

        val signature = registerResponse.workerConfiguration.payloadSignature

        while (currentCoroutineContext().isActive) {
            val response = api.getBucket(
                GetBucketBody(
                    workerId = workerId,
                    payloadSignature = signature,
                    workerCapabilities = emptyList() // TODO do we have to pass something?
                )
            ).getOrElse { throwable ->
                throw throwable
                    .toQueueProblem("Polling the Emcee queue for available buckets")
                    .asRuntimeException()
            }

            when (response) {
                is GetBucketResponse.Dequeued -> emit(
                    TestJobProducer.Job(
                        workerId = workerId,
                        payloadSignature = signature,
                        bucket = response.bucket
                    )
                )
                is GetBucketResponse.NoBucket -> delay(response.checkAfter)
            }
        }
    }

    private fun Throwable.toQueueProblem(context: String) = Problem(
        shortDescription = "Connection to Emcee queue failed",
        context = context,
        possibleSolutions = listOf("Check if Emcee queue is running and available"),
        throwable = this
    )
}
