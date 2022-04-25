package com.avito.emcee.worker

import com.avito.emcee.queue.Bucket
import com.squareup.moshi.JsonClass
import dev.zacsweers.moshix.sealed.annotations.TypeLabel

@JsonClass(generateAdapter = true, generator = "sealed:caseId")
public sealed class GetBucketResponse {

    @JsonClass(generateAdapter = true)
    @TypeLabel("checkAgainLater")
    public data class NoBucket(val checkAfter: Int) : GetBucketResponse()

    @JsonClass(generateAdapter = true)
    @TypeLabel("bucketDequeued")
    public data class Dequeued(val bucket: Bucket) : GetBucketResponse()

    internal companion object
}
