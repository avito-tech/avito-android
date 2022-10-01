package com.avito.emcee.worker

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class Config(
    /**
     * Worker starts a REST server on this port
     */
    val workerPort: Int,
    /**
     * Url where worker will ask for test buckets
     */
    val queueUrl: String,
    /**
     * ANDROID_HOME directory
     * If null we will search it at the system env ANDROID_HOME
     */
    val androidSdkPath: String?,
)
