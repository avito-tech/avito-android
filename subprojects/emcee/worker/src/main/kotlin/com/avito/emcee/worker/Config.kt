package com.avito.emcee.worker

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.nio.file.Path
import kotlin.io.path.exists

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
     */
    @Json(name = "androidSdkPath")
    val androidSdkPathString: String,

    /**
     * Emulator data by `sdk`, `type`
     */
    val avd: Set<Avd>,
) {
    @JsonClass(generateAdapter = true)
    public data class Avd(
        val sdk: Int,
        val type: String,
        val emulatorFileName: String,
        val sdCardFileName: String
    )

    val androidSdkPath: Path
        get() {
            val androidSdkHomePath = Path.of(androidSdkPathString)
            require(androidSdkHomePath.exists()) {
                "Incorrect config. $androidSdkPathString doesn't exist"
            }
            return androidSdkHomePath
        }
}
