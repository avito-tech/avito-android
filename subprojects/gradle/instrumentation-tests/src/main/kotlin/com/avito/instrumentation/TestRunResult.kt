package com.avito.instrumentation

import com.avito.composite_exception.composeWith
import com.avito.instrumentation.report.HasFailedTestDeterminer
import com.avito.instrumentation.report.HasNotReportedTestsDeterminer
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.Status

data class TestRunResult(
    val reportedTests: List<SimpleRunTest>,
    val failed: HasFailedTestDeterminer.Result,
    val notReported: HasNotReportedTestsDeterminer.Result
) {
    sealed class Verdict {
        abstract val message: String

        data class Success(
            override val message: String
        ) : Verdict()

        data class Failure(
            override val message: String,
            val prettifiedDetails: Details,
            val cause: Throwable? = null
        ) : Verdict() {

            data class Details(
                val lostTests: Set<Test>,
                val failedTests: Set<Test>
            ) {
                operator fun plus(other: Details): Details {
                    return Details(
                        lostTests = lostTests + other.lostTests,
                        failedTests = failedTests + other.failedTests
                    )
                }

                data class Test(val name: String, val devices: Set<String>)
            }
        }
    }

    val verdict: Verdict
        get() {
            val failedVerdict = getFailedVerdict()
            val notReportedVerdict = getNotReportedVerdict()
            return when {
                failedVerdict is Verdict.Success && notReportedVerdict is Verdict.Success -> {
                    Verdict.Success(
                        """
                            |${failedVerdict.message}.
                            |${notReportedVerdict.message}.""".trimMargin()
                    )
                }
                failedVerdict is Verdict.Failure && notReportedVerdict is Verdict.Failure -> {
                    val message = "${failedVerdict.message}. \n ${notReportedVerdict.message}"
                    Verdict.Failure(
                        message = message,
                        prettifiedDetails = failedVerdict.prettifiedDetails + notReportedVerdict.prettifiedDetails,
                        cause = failedVerdict.cause.composeWith(notReportedVerdict.cause)
                    )
                }
                failedVerdict is Verdict.Failure -> failedVerdict
                notReportedVerdict is Verdict.Failure -> notReportedVerdict
                else -> throw IllegalStateException("Must be unreach")
            }
        }

    private fun getNotReportedVerdict() = when (notReported) {
        is HasNotReportedTestsDeterminer.Result.AllTestsReported -> Verdict.Success("All tests reported")
        is HasNotReportedTestsDeterminer.Result.DetermineError -> Verdict.Failure(
            message = "Failed. Couldn't determine not reported tests",
            prettifiedDetails = Verdict.Failure.Details(
                lostTests = emptySet(),
                failedTests = emptySet()
            ),
            cause = notReported.exception
        )
        is HasNotReportedTestsDeterminer.Result.HasNotReportedTests -> Verdict.Failure(
            message = "Failed. There are ${notReported.lostTests.size} not reported tests.",
            prettifiedDetails = Verdict.Failure.Details(
                lostTests = notReported.getLostFailureDetailsTests(),
                failedTests = emptySet()
            ),
            cause = null
        )
    }

    private fun HasNotReportedTestsDeterminer.Result.HasNotReportedTests.getLostFailureDetailsTests(): Set<Verdict.Failure.Details.Test> =
        lostTests.groupBy({ test -> test.name }, { test -> test.device.name })
            .map { (testName, devices) ->
                Verdict.Failure.Details.Test(
                    name = testName.name,
                    devices = devices.toSet()
                )
            }
            .toSet()

    private fun getFailedVerdict() = when (failed) {
        is HasFailedTestDeterminer.Result.NoFailed -> Verdict.Success("No failed tests")
        is HasFailedTestDeterminer.Result.DetermineError -> Verdict.Failure(
            message = "Failed. Couldn't determine failed tests",
            prettifiedDetails = Verdict.Failure.Details(
                lostTests = emptySet(),
                failedTests = emptySet()
            ),
            cause = failed.throwable
        )
        is HasFailedTestDeterminer.Result.Failed -> {
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
    }

    private fun HasFailedTestDeterminer.Result.Failed.getNotSuppressedFailedDetailsTests(): Set<Verdict.Failure.Details.Test> =
        notSuppressed.groupBy({ test -> test.name }, { test -> test.deviceName })
            .map { (testName, devices) -> Verdict.Failure.Details.Test(testName, devices.toSet()) }
            .toSet()

    val testsDuration
        get() = reportedTests.sumBy { it.lastAttemptDurationInSeconds }

    fun testCount(): Int = reportedTests.size + notReported.lostTests.size

    fun successCount(): Int =
        reportedTests.filter { it.status is Status.Success || it.status is Status.Manual }.size

    fun skippedCount(): Int = reportedTests.filter { it.status is Status.Skipped }.size

    fun failureCount(): Int = failed.count()

    fun notReportedCount(): Int = notReported.lostTests.size

}
