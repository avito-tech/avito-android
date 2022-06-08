package com.avito.emcee.worker

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class Config(
    /**
     * Helps Queue to identify worker's interactions
     */
    val workerId: String,
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
    /**
     * Emulator data by [sdk], [type]
     */
    val avd: Set<Avd>
) {
    @JsonClass(generateAdapter = true)
    public data class Avd(
        val sdk: Int,
        val type: String,
        val emulatorFileName: String,
        val sdCardFileName: String
    )
}
