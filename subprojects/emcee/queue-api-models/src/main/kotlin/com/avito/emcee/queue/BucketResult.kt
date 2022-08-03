package com.avito.emcee.queue

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

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
            val udid: String,
            @Json(name = "duration")
            val durationSec: Int,
            val exceptions: List<Exception>,
            val hostName: String,
            val logs: List<Log>,
            val startTime: StartTime,
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

            @JsonClass(generateAdapter = true)
            public data class StartTime(val date: Long)
        }
    }

    public companion object
}
