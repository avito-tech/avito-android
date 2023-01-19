package com.avito.emcee.worker.configuration

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class WorkerConfiguration(
    val payloadSignature: PayloadSignature,
    val portRange: PortRange,
)
