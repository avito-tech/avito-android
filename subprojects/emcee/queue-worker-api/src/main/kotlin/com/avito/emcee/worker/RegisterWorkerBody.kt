package com.avito.emcee.worker

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class RegisterWorkerBody(
    val workerId: String,
    @Json(name = "workerRestAddress")
    val workerRestUrl: String,
) {
    public val workerCapabilities: List<Nothing> = emptyList()
}
