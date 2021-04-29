package com.avito.instrumentation.internal.finalizer.verdict

import com.avito.composite_exception.composeWith
import com.avito.report.model.TestName

internal class VerdictDeterminerImpl : VerdictDeterminer {

    override fun determine(
        failed: HasFailedTestDeterminer.Result,
        notReported: HasNotReportedTestsDeterminer.Result
    ): Verdict {
        val failedVerdict = getFailedVerdict(failed)
        val notReportedVerdict = getNotReportedVerdict(notReported)
        return when {
            failedVerdict is Verdict.Success && notReportedVerdict is Verdict.Success ->
                Verdict.Success(
                    """
                        |${failedVerdict.message}.
                        |${notReportedVerdict.message}.""".trimMargin()
                )
            failedVerdict is Verdict.Failure && notReportedVerdict is Verdict.Failure -> {
                val message = "${failedVerdict.message}. \n${notReportedVerdict.message}"
                Verdict.Failure(
                    message = message,
                    prettifiedDetails = failedVerdict.prettifiedDetails + notReportedVerdict.prettifiedDetails,
                    cause = failedVerdict.cause.composeWith(notReportedVerdict.cause)
                )
            }
            failedVerdict is Verdict.Failure -> failedVerdict
            notReportedVerdict is Verdict.Failure -> notReportedVerdict
            else -> throw IllegalStateException("Must be unreached")
        }
    }

    private fun getNotReportedVerdict(notReported: HasNotReportedTestsDeterminer.Result): Verdict = when (notReported) {
        is HasNotReportedTestsDeterminer.Result.AllTestsReported -> Verdict.Success("All tests reported")
        is HasNotReportedTestsDeterminer.Result.HasNotReportedTests -> Verdict.Failure(
            message = "Failed. There are ${notReported.lostTests.size} not reported tests.",
            prettifiedDetails = Verdict.Failure.Details(
                lostTests = notReported.getLostFailureDetailsTests(),
                failedTests = emptySet()
            ),
            cause = null
        )
    }

    private fun getFailedVerdict(failed: HasFailedTestDeterminer.Result): Verdict = when (failed) {
        is HasFailedTestDeterminer.Result.NoFailed -> Verdict.Success("No failed tests")
        is HasFailedTestDeterminer.Result.Failed ->
            if (failed.notSuppressedCount > 0) {
                Verdict.Failure(
                    message = "Failed. There are ${failed.notSuppressedCount} unsuppressed failed tests",
                    prettifiedDetails = Verdict.Failure.Details(
                        lostTests = emptySet(),
                        failedTests = failed.getNotSuppressedFailedDetailsTests()
                    ),
                    cause = null
                )
            } else {
                Verdict.Success("Success. All failed tests suppressed by ${failed.suppression}")
            }
    }

    private fun HasNotReportedTestsDeterminer.Result.HasNotReportedTests.getLostFailureDetailsTests() =
        lostTests.groupBy({ test -> test.name }, { test -> test.device.name })
            .map { (testName, devices) ->
                Verdict.Failure.Details.Test(
                    name = testName,
                    devices = devices.toSet()
                )
            }
            .toSet()

    private fun HasFailedTestDeterminer.Result.Failed.getNotSuppressedFailedDetailsTests() =
        notSuppressed.groupBy(
            { test -> TestName(test.className, test.methodName) },
            { test -> test.deviceName }
        )
            .map { (testName, devices) ->
                Verdict.Failure.Details.Test(
                    name = testName,
                    devices = devices.toSet()
                )
            }
            .toSet()
}
