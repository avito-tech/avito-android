package com.avito.emcee.worker.configuration

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class PortRange(
    val from: Int,
    val to: Int,
)
