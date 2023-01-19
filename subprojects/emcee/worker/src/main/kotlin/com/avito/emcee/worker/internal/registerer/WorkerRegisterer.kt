package com.avito.emcee.worker.internal.registerer

import com.avito.emcee.worker.configuration.PayloadSignature

internal interface WorkerRegisterer {

    suspend fun register(onWorkerPortAvailable: (Int) -> Unit): PayloadSignature
}
