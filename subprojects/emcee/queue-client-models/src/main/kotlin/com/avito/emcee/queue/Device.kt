package com.avito.emcee.queue

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class Device(
    /**
     * Should be deleted
     */
    @Json(name = "deviceType")
    val type: String,
    /**
     * Representing an emulator API version: 21,22,29,30 etc.
     */
    val runtime: String,
)
