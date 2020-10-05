package com.avito.test.summary

import org.funktionale.tries.Try

internal interface FlakyTestReporter {
    fun reportSummary(
        info: List<FlakyInfo>
    ): Try<Unit>
}
