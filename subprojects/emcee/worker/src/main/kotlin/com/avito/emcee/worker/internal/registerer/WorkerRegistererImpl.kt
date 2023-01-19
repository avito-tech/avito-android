package com.avito.emcee.worker.internal.registerer

import com.avito.android.Problem
import com.avito.android.asRuntimeException
import com.avito.emcee.worker.RegisterWorkerBody
import com.avito.emcee.worker.UpdateWorkerDetailsBody
import com.avito.emcee.worker.WorkerQueueApi
import com.avito.emcee.worker.configuration.PayloadSignature
import com.avito.emcee.worker.internal.networking.WorkerHostAddressResolver

internal class WorkerRegistererImpl(
    private val queueApi: WorkerQueueApi,
    private val workerId: String,
    private val workerHostAddressResolver: WorkerHostAddressResolver,
) : WorkerRegisterer {

    override suspend fun register(onWorkerPortAvailable: (Int) -> Unit): PayloadSignature {

        val registrationResponse = queueApi.registerWorker(
            RegisterWorkerBody(workerId = workerId)
        ).getOrElse {
            throw it.toQueueProblem("Registering a new worker at startup").asRuntimeException()
        }

        val requiredPort = registrationResponse.workerConfiguration.portRange.from
        onWorkerPortAvailable(requiredPort)

        val workerRestUrl = workerHostAddressResolver.getWorkerRestUrl(requiredPort)
        queueApi.updateWorkerDetails(
            UpdateWorkerDetailsBody(
                workerId = workerId,
                workerRestUrl = workerRestUrl.toString()
            )
        )

        return registrationResponse.workerConfiguration.payloadSignature
    }

    private fun Throwable.toQueueProblem(context: String) = Problem(
        shortDescription = "Connection to Emcee queue failed",
        context = context,
        possibleSolutions = listOf("Check if Emcee queue is running and available"),
        throwable = this
    )
}
