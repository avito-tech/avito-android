package com.avito.emcee.client.internal.result

import com.avito.emcee.queue.TestEntry

internal sealed interface JobResult {

    object Success : JobResult

    data class Failure(
        val failedTests: List<TestEntry>
    ) : JobResult
}
