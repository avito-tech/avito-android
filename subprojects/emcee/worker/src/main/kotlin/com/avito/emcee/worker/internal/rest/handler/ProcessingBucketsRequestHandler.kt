package com.avito.emcee.worker.internal.rest.handler

import com.avito.emcee.worker.internal.rest.HttpMethod
import com.avito.emcee.worker.internal.storage.ProcessingBucketsStorage

internal class ProcessingBucketsRequestHandler(
    private val bucketsStorage: ProcessingBucketsStorage,
) : RequestHandler(HttpMethod.POST, "/currentlyProcessingBuckets", {

    val bucketIds = bucketsStorage.getAll().joinToString(
        separator = ",",
        prefix = "[",
        postfix = "]",
    ) { "\"${it.bucketId}\"" }

    """
        |{ 
        |    "bucketIds": $bucketIds
        |}
    """.trimMargin()
})
