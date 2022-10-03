package com.avito.emcee.worker.internal.identifier

internal interface WorkerIdProvider {
    fun provide(): String
}
