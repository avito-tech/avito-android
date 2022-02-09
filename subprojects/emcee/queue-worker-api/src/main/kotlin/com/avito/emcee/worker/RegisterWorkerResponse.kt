package com.avito.emcee.worker

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class RegisterWorkerResponse(
    val workerConfiguration: WorkerConfiguration,
    val numberOfSimulators: Int,
    val payloadSignature: String,
) {

    public data class WorkerConfiguration(
        val globalAnalyticsConfiguration: Any,
        val kibanaConfiguration: Any,
        val metadata: Map<String, String>,
        val persistentMetricsJobId: String,
        val statsdConfiguration: Any,
    )
}
