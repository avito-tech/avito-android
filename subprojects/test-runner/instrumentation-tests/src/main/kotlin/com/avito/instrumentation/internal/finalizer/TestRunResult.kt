package com.avito.instrumentation.internal.finalizer

import com.avito.instrumentation.internal.finalizer.verdict.HasFailedTestDeterminer
import com.avito.instrumentation.internal.finalizer.verdict.HasNotReportedTestsDeterminer
import com.avito.report.model.SimpleRunTest

internal data class TestRunResult(
    val reportedTests: List<SimpleRunTest>,
    val failed: HasFailedTestDeterminer.Result,
    val notReported: HasNotReportedTestsDeterminer.Result
)
