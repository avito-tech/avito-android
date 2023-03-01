package com.avito.emcee.worker

import com.avito.android.Problem
import com.avito.android.asRuntimeException
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.nio.file.Path
import kotlin.io.path.exists

@JsonClass(generateAdapter = true)
public data class Config(
    /**
     * Queue configuration
     */
    val queue: QueueConfig,
    /**
     * ANDROID_HOME directory
     */
    @Json(name = "androidSdkPath")
    val androidSdkPathString: String,

    /**
     * Emulator data by `sdk`, `type`
     */
    val configurations: Set<AvdConfiguration>,
) {
    @JsonClass(generateAdapter = true)
    public data class AvdConfiguration(
        val sdk: Int,
        val type: String,
        val emulatorFileName: String,
        val sdCardFileName: String
    )

    @JsonClass(generateAdapter = true)
    public data class QueueConfig(
        val url: String,
        val triesCount: Int,
        val retryDelayMs: Long,
    )

    val androidSdkPath: Path
        get() {
            val androidSdkHomePath = Path.of(androidSdkPathString)
            if (!androidSdkHomePath.exists()) {
                throw Problem(
                    shortDescription = "Provided Android SDK path does not exist",
                    context = "Parsing worker configuration",
                    possibleSolutions = listOf("""
                        Ensure that Android SDK is available at the path, specified in the worker configuration file.
                    """.trimIndent())
                ).asRuntimeException()
            }
            return androidSdkHomePath
        }
}
