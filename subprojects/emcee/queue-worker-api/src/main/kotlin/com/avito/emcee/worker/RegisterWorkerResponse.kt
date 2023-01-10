package com.avito.emcee.worker

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class RegisterWorkerResponse(
    val workerConfiguration: WorkerConfiguration,
) {

    @JsonClass(generateAdapter = true)
    public data class WorkerConfiguration(
        val payloadSignature: String,
    )
}
