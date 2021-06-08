package com.avito.runner.finalizer.verdict

import com.avito.report.model.Flakiness
import com.avito.report.model.SimpleRunTest

internal class LegacyFailedTestDeterminer(
    private val suppressFailure: Boolean,
    private val suppressFlaky: Boolean
) : HasFailedTestDeterminer {

    override fun determine(runResult: List<SimpleRunTest>): HasFailedTestDeterminer.Result {
        val failedTests = runResult.filter { !it.status.isSuccessful }
        val hasFailedTests = failedTests.isNotEmpty()

        return when {
            hasFailedTests ->
                when {
                    suppressFailure -> {
                        HasFailedTestDeterminer.Result.Failed(
                            failed = failedTests,
                            suppression = HasFailedTestDeterminer.Result.Failed.Suppression.SuppressedAll(
                                tests = failedTests
                            )
                        )
                    }
                    suppressFlaky -> {
                        HasFailedTestDeterminer.Result.Failed(
                            failed = failedTests,
                            suppression = HasFailedTestDeterminer.Result.Failed.Suppression.SuppressedFlaky(
                                tests = failedTests.filter { it.flakiness is Flakiness.Flaky }
                            )
                        )
                    }
                    else -> HasFailedTestDeterminer.Result.Failed(failed = failedTests)
                }
            else -> HasFailedTestDeterminer.Result.NoFailed
        }
    }
}
