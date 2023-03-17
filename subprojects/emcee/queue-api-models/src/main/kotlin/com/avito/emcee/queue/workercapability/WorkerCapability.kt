package com.avito.emcee.queue.workercapability

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public open class WorkerCapability(
    public val name: String,
    public val value: String
)
