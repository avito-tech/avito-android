package com.avito.emcee.worker

import com.avito.emcee.queue.workercapability.WorkerCapability
import com.avito.emcee.queue.workercapability.defaultCapabilities
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class UpdateWorkerDetailsBody(
    val workerId: String,
    val workerRestUrl: String,
    val workerCapabilities: List<WorkerCapability> = defaultCapabilities()
)
