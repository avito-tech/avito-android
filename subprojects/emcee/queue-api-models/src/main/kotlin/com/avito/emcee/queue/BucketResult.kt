package com.avito.emcee.queue

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlin.time.Duration

@JsonClass(generateAdapter = true)
public data class BucketResult(
    @Json(name = "testDestination")
    val device: DeviceConfiguration,
    val unfilteredResults: List<UnfilteredResult>
) {
    @JsonClass(generateAdapter = true)
    public data class UnfilteredResult(
        val testEntry: TestEntry,
        val testRunResults: List<TestRunResult>
    ) {
        @JsonClass(generateAdapter = true)
        public data class TestRunResult(
            @Json(name = "udid")
            val uuid: String,
            val duration: Duration,
            val exceptions: List<Exception>,
            val hostName: String,
            val logs: List<Log>,
            val startTime: Long,
            val succeeded: Boolean
        ) {
            @JsonClass(generateAdapter = true)
            public data class Exception(
                val filePathInProject: String,
                val lineNumber: Int,
                val reason: String,
                val relatedTestName: TestName,
            )

            @JsonClass(generateAdapter = true)
            public data class Log(val contents: String)
        }
    }

    public companion object
}
