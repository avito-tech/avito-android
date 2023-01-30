package com.avito.emcee.worker

import com.avito.emcee.queue.BucketResultContainer
import com.avito.emcee.worker.configuration.PayloadSignature
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class SendBucketResultBody(
    val bucketId: String,
    val payloadSignature: PayloadSignature,
    val workerId: String,
    val bucketResultContainer: BucketResultContainer,
)
