package com.avito.emcee.queue

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class TestName(
    val className: String,
    val methodName: String
)
