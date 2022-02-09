package com.avito.emcee.worker

import com.avito.emcee.queue.BuildArtifacts
import com.avito.emcee.queue.Device
import com.avito.emcee.queue.TestEntry
import com.avito.emcee.queue.TestExecutionBehavior
import com.avito.emcee.queue.TestTimeoutConfiguration
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import dev.zacsweers.moshix.sealed.annotations.TypeLabel

@JsonClass(generateAdapter = true, generator = "sealed:caseId")
public sealed class GetBucketResponse {

    @JsonClass(generateAdapter = true)
    @TypeLabel("checkAgainLater")
    public data class NoBucket(val checkAfter: Int) : GetBucketResponse()

    @JsonClass(generateAdapter = true)
    @TypeLabel("bucketDequeued")
    public data class Dequeued(
        val analyticsConfiguration: Any,
        val bucketId: String,
        val buildArtifacts: BuildArtifacts,
        @Json(name = "testDestination")
        val device: Device,
        val testEntries: List<TestEntry>,
        val testExecutionBehavior: TestExecutionBehavior,
        val testTimeoutConfiguration: TestTimeoutConfiguration,
    ) : GetBucketResponse()
}
