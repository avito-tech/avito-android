package com.avito.instrumentation

import com.avito.instrumentation.report.HasFailedTestDeterminer
import com.avito.instrumentation.report.HasNotReportedTestsDeterminer
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.Status
import java.io.PrintStream
import java.io.PrintWriter

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

        data class Failed(
            override val message: String,
            val throwable: Throwable
        ) : Verdict()
    }

    val verdict: Verdict
        get() {
            val failedVerdict = getFailedVerdict()
            val notReportedVerdict = getNotReportedVerdict()
            return when {
                failedVerdict is Verdict.Success && notReportedVerdict is Verdict.Success -> {
                    Verdict.Success(
                        "${failedVerdict.message}. ${notReportedVerdict.message}"
                    )
                }
                failedVerdict is Verdict.Failed && notReportedVerdict is Verdict.Failed -> {
                    val message = "${failedVerdict.message}. \n ${notReportedVerdict.message}"
                    Verdict.Failed(
                        message,
                        CompositeException(
                            message,
                            arrayOf(failedVerdict.throwable, notReportedVerdict.throwable)
                        )
                    )
                }
                failedVerdict is Verdict.Failed -> failedVerdict
                notReportedVerdict is Verdict.Failed -> notReportedVerdict
                else -> throw IllegalStateException("Must be unreach")
            }
        }

    private fun getNotReportedVerdict() = when (notReported) {
        is HasNotReportedTestsDeterminer.Result.AllTestsReported -> Verdict.Success("All tests reported")
        is HasNotReportedTestsDeterminer.Result.DetermineError -> Verdict.Failed(
            "Failed. Couldn't determine not reported tests",
            notReported.exception
        )
        is HasNotReportedTestsDeterminer.Result.HasNotReportedTests -> Verdict.Failed(
            "Failed. There are ${notReported.lostTests.size} not reported tests.",
            IllegalStateException("Not reported tests:\n ${notReported.lostTests.joinToString { it.name.toString() }}")
        )
    }

    private fun getFailedVerdict() = when (failed) {
        is HasFailedTestDeterminer.Result.NoFailed -> Verdict.Success("No failed tests")
        is HasFailedTestDeterminer.Result.DetermineError -> Verdict.Failed(
            "Failed. Couldn't determine failed tests",
            failed.throwable
        )
        is HasFailedTestDeterminer.Result.Failed -> {
            if (failed.notSuppressedCount > 0) {
                Verdict.Failed(
                    "Failed. There are ${failed.notSuppressedCount} unsuppressed failed tests",
                    IllegalStateException("Unsuppressed failed tests:\\n${failed.notSuppressed.lineByLine()}")
                )
            } else {
                Verdict.Success("Success. All failed tests suppressed by ${failed.suppression}")
            }
        }
    }

    val testsDuration
        get() = reportedTests.sumBy { it.lastAttemptDurationInSeconds }

    fun testCount(): Int = reportedTests.size + notReported.lostTests.size

    fun successCount(): Int =
        reportedTests.filter { it.status is Status.Success || it.status is Status.Manual }.size

    fun skippedCount(): Int = reportedTests.filter { it.status is Status.Skipped }.size

    fun failureCount(): Int = failed.count()

    fun notReportedCount(): Int = notReported.lostTests.size

    private fun Iterable<SimpleRunTest>.lineByLine() = joinToString(separator = "\n") { it.name }

}

private class CompositeException(
    message: String,
    private val throwables: Array<Throwable>
) : RuntimeException(message) {

    override fun printStackTrace() {
        if (throwables.isEmpty()) {
            super.printStackTrace()
        } else {
            throwables.forEach {
                it.printStackTrace()
            }
        }
    }

    override fun printStackTrace(s: PrintStream) {
        if (throwables.isEmpty()) {
            super.printStackTrace(s)
        } else {
            throwables.forEach {
                it.printStackTrace(s)
            }
        }
    }

    override fun printStackTrace(s: PrintWriter) {
        if (throwables.isEmpty()) {
            super.printStackTrace(s)
        } else {
            throwables.forEach {
                it.printStackTrace(s)
            }
        }
    }
}
