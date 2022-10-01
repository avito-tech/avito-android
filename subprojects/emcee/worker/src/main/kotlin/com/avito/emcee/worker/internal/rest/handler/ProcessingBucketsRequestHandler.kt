package com.avito.emcee.worker.internal.rest.handler

import com.avito.emcee.worker.internal.rest.model.CurrentlyProcessingBucketsResponse
import com.avito.emcee.worker.internal.storage.ProcessingBucketsStorage
import io.ktor.http.HttpMethod

internal class ProcessingBucketsRequestHandler(
    private val bucketsStorage: ProcessingBucketsStorage,
) : RequestHandler<CurrentlyProcessingBucketsResponse>(HttpMethod.Post, "/currentlyProcessingBuckets", {

    CurrentlyProcessingBucketsResponse(bucketsStorage.getAll().map { it.bucketId })
})
