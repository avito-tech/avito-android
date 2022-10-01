package com.avito.emcee.queue

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class Bucket(
    val bucketId: String,
    val payloadContainer: PayloadContainer,
    val analyticsConfiguration: Any,
    val workerCapabilityRequirements: List<String>
) {
    public companion object
}
