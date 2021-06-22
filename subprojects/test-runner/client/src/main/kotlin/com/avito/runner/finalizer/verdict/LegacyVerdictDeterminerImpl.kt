package com.avito.runner.finalizer.verdict

import com.avito.composite_exception.composeWith

internal class LegacyVerdictDeterminerImpl : LegacyVerdictDeterminer {

    override fun determine(
        failed: HasFailedTestDeterminer.Result,
        notReported: HasNotReportedTestsDeterminer.Result
    ): LegacyVerdict {
        val failedVerdict = getFailedVerdict(failed)
        val notReportedVerdict = getNotReportedVerdict(notReported)
        return when {
            failedVerdict is LegacyVerdict.Success && notReportedVerdict is LegacyVerdict.Success ->
                LegacyVerdict.Success(
                    """
                        |${failedVerdict.message}.
                        |${notReportedVerdict.message}.""".trimMargin()
                )
            failedVerdict is LegacyVerdict.Failure && notReportedVerdict is LegacyVerdict.Failure -> {
                val message = "${failedVerdict.message}. \n${notReportedVerdict.message}"
                LegacyVerdict.Failure(
                    message = message,
                    prettifiedDetails = failedVerdict.prettifiedDetails + notReportedVerdict.prettifiedDetails,
                    cause = failedVerdict.cause.composeWith(notReportedVerdict.cause)
                )
            }
            failedVerdict is LegacyVerdict.Failure -> failedVerdict
            notReportedVerdict is LegacyVerdict.Failure -> notReportedVerdict
            else -> throw IllegalStateException("Must be unreached")
        }
    }

    private fun getNotReportedVerdict(notReported: HasNotReportedTestsDeterminer.Result): LegacyVerdict =
        when (notReported) {
            is HasNotReportedTestsDeterminer.Result.AllTestsReported -> LegacyVerdict.Success("All tests reported")
            is HasNotReportedTestsDeterminer.Result.HasNotReportedTests -> LegacyVerdict.Failure(
                message = "Failed. There are ${notReported.lostTests.size} not reported tests.",
                prettifiedDetails = LegacyVerdict.Failure.Details(
                    lostTests = notReported.getLostFailureDetailsTests(),
                    failedTests = emptySet()
                ),
                cause = null
            )
        }

    private fun getFailedVerdict(failed: HasFailedTestDeterminer.Result): LegacyVerdict = when (failed) {
        is HasFailedTestDeterminer.Result.NoFailed -> LegacyVerdict.Success("No failed tests")
        is HasFailedTestDeterminer.Result.Failed ->
            if (failed.notSuppressedCount > 0) {
                LegacyVerdict.Failure(
                    message = "Failed. There are ${failed.notSuppressedCount} unsuppressed failed tests",
                    prettifiedDetails = LegacyVerdict.Failure.Details(
                        lostTests = emptySet(),
                        failedTests = failed.getNotSuppressedFailedDetailsTests()
                    ),
                    cause = null
                )
            } else {
                LegacyVerdict.Success("Success. All failed tests suppressed by ${failed.suppression}")
            }
    }

    private fun HasNotReportedTestsDeterminer.Result.HasNotReportedTests.getLostFailureDetailsTests() =
        lostTests.groupBy({ test -> test.name }, { test -> test.device.name })
            .map { (testName, devices) ->
                LegacyVerdict.Failure.Details.Test(
                    name = testName,
                    devices = devices.toSet()
                )
            }
            .toSet()

    private fun HasFailedTestDeterminer.Result.Failed.getNotSuppressedFailedDetailsTests() =
        notSuppressed.groupBy(
            { test -> test.name },
            { test -> test.deviceName }
        )
            .map { (testName, devices) ->
                LegacyVerdict.Failure.Details.Test(
                    name = testName,
                    devices = devices.toSet()
                )
            }
            .toSet()
}
