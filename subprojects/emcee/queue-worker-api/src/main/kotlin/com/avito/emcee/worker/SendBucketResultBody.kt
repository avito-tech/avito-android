package com.avito.emcee.worker

import com.avito.emcee.queue.Device
import com.avito.emcee.queue.TestEntry
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class SendBucketResultBody(
    val bucketId: String,
    val payloadSignature: String,
    val workerId: String,
    val bucketResult: BucketResult,
) {
    @JsonClass(generateAdapter = true)
    public data class BucketResult(
        @Json(name = "testDestination")
        val device: Device,
        val unfilteredResults: List<UnfilteredResult>
    ) {
        @JsonClass(generateAdapter = true)
        public data class UnfilteredResult(
            val testEntry: TestEntry,
            val testRunResult: List<TestRunResult>
        ) {
            @JsonClass(generateAdapter = true)
            public data class TestRunResult(
                /**
                 * in sec
                 */
                val duration: Int,
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
                    val relatedTestName: RelatedTestName,
                )

                @JsonClass(generateAdapter = true)
                public data class Log(val contents: String)

                @JsonClass(generateAdapter = true)
                public data class RelatedTestName(val className: String, val methodName: String)

                @JsonClass(generateAdapter = true)
                public data class StartTime(val date: Long)
            }
        }
    }
}
