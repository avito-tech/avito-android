package com.avito.emcee.client.internal.result

import com.avito.emcee.queue.TestEntry

internal class JobResultHasFailedTestsException(
    failedTests: List<TestEntry>
) : RuntimeException(
    "These tests has been failed:\n${
        failedTests.map { it.name }.joinToString(separator = "\n") { "${it.className}#${it.methodName}" }
    }"
)
