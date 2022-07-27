package com.avito.emcee.worker.internal.storage

import com.avito.emcee.queue.Bucket

internal interface ProcessingBucketsStorage {
    fun add(bucket: Bucket)
    fun remove(bucket: Bucket)
    fun getAll(): Set<Bucket>
}
