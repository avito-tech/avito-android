package com.avito.emcee.worker

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class Config(
    /**
     * Helps Queue to identify worker's interactions
     */
    val workerId: String,
    /**
     * Helps Queue to interact with worker
     */
    val restAddress: String,
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
