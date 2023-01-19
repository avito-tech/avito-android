package com.avito.emcee.worker

import com.avito.emcee.worker.configuration.PayloadSignature
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class GetBucketBody(
    val workerId: String,
    val payloadSignature: PayloadSignature,
    val workerCapabilities: List<Any> = emptyList(),
)
