package com.avito.emcee.worker

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class RegisterWorkerBody(
    val workerId: String,
)
