package com.avito.emcee.worker

import com.avito.emcee.worker.configuration.WorkerConfiguration
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class RegisterWorkerResponse(
    val workerConfiguration: WorkerConfiguration,
)
