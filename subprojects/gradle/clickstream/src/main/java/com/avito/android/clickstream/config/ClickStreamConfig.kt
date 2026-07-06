package com.avito.android.clickstream.config

import java.io.Serializable

public data class ClickStreamConfig(
    val serviceUrl: String,
    val readTimeOutInSeconds: Long,
    val connectTimeOutInSeconds: Long,
    val useLegacyEndpoint: Boolean,
) : Serializable
