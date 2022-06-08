package com.avito.emcee.worker

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class RegisterWorkerResponse(
    val workerConfiguration: WorkerConfiguration,
    val caseId: String,
) {

    @JsonClass(generateAdapter = true)
    public data class WorkerConfiguration(
        val globalAnalyticsConfiguration: Any,
        val maximumCacheSize: Long,
        val numberOfSimulators: Int,
        val portRange: Any,
        val payloadSignature: String,
        val maximumCacheTTL: Long,
    )
}
