package com.avito.runner.finalizer

import com.avito.report.model.SimpleRunTest
import com.avito.runner.finalizer.verdict.HasFailedTestDeterminer
import com.avito.runner.finalizer.verdict.HasNotReportedTestsDeterminer

internal data class TestRunResult(
    val reportedTests: List<SimpleRunTest>,
    val failed: HasFailedTestDeterminer.Result,
    val notReported: HasNotReportedTestsDeterminer.Result
)
