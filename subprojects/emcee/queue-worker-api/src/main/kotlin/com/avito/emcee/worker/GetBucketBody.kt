package com.avito.emcee.worker

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class GetBucketBody(
    val workerId: String,
    val workerCapabilities: List<Any>,
    val payloadSignature: String
)
