package com.avito.emcee.worker.rest

import com.avito.emcee.queue.Bucket
import com.avito.emcee.worker.internal.storage.ProcessingBucketsStorage

internal class NoOpProcessingBucketsStorage : ProcessingBucketsStorage {

    override fun add(bucket: Bucket) {
        // no-op
    }

    override fun remove(bucket: Bucket) {
        // no-op
    }

    override fun getAll(): Set<Bucket> = emptySet()
}
