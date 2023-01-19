package com.avito.emcee.worker

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class UpdateWorkerDetailsBody(
    val workerId: String,
    val workerRestUrl: String,
    val workerCapabilities: List<Any> = emptyList()
)
