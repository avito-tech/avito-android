package com.avito.runner.finalizer.verdict

import com.avito.report.model.AndroidTest
import com.avito.report.model.Flakiness
import com.avito.report.model.TestStaticData
import com.avito.test.model.TestCase
import com.avito.time.TimeProvider

internal class VerdictDeterminerImpl(
    private val suppressFlaky: Boolean,
    private val suppressFailure: Boolean,
    private val timeProvider: TimeProvider
) : VerdictDeterminer {

    override fun determine(
        initialTestSuite: Set<TestStaticData>,
        testResults: Collection<AndroidTest>
    ): Verdict {

        val failedTests = getFailedTests(testResults)

        val notReportedTests = getNotReportedTests(
            initialTestSuite = initialTestSuite,
            testResults = testResults
        )

        return when {
            suppressFailure ->
                if (failedTests.isNotEmpty() || notReportedTests.isNotEmpty()) {
                    Verdict.Success.Suppressed(
                        testResults = testResults,
                        notReportedTests = notReportedTests,
                        failedTests = failedTests
                    )
                } else {
                    Verdict.Success.OK(testResults)
                }
            suppressFlaky -> {
                val (flaky, notFlaky) = failedTests.partition { it.flakiness is Flakiness.Flaky }

                val hasFailedTestsNotMarkedAsFlaky = notFlaky.isNotEmpty()

                if (hasFailedTestsNotMarkedAsFlaky || notReportedTests.isNotEmpty()) {
                    Verdict.Failure(
                        testResults = testResults,
                        notReportedTests = notReportedTests,
                        unsuppressedFailedTests = notFlaky.toSet()
                    )
                } else {
                    Verdict.Success.Suppressed(
                        testResults = testResults,
                        notReportedTests = notReportedTests,
                        failedTests = flaky.toSet()
                    )
                }
            }
            else -> if (failedTests.isNotEmpty() || notReportedTests.isNotEmpty()) {
                Verdict.Failure(
                    testResults = testResults,
                    notReportedTests = notReportedTests,
                    unsuppressedFailedTests = failedTests
                )
            } else {
                Verdict.Success.OK(testResults)
            }
        }
    }

    private fun getFailedTests(testVerdicts: Collection<AndroidTest>): Set<TestStaticData> {
        val completedWithIncident =
            testVerdicts.filterIsInstance<AndroidTest.Completed>().filter { it.incident != null }
        val infrastructureError = testVerdicts.filterIsInstance<AndroidTest.Lost>()

        return (completedWithIncident + infrastructureError).toSet()
    }

    private fun getNotReportedTests(
        initialTestSuite: Set<TestStaticData>,
        testResults: Collection<AndroidTest>
    ): Set<AndroidTest.Lost> {
        return initialTestSuite
            .minus(testResults) { TestCase(it.name, it.device) }
            .map {
                AndroidTest.Lost.createWithoutInfo(
                    testStaticData = it,
                    currentTimeSec = timeProvider.nowInSeconds()
                )
            }
            .toSet()
    }

    private fun <T, R> Collection<T>.minus(elements: Collection<T>, selector: (T) -> R?) =
        filter { t -> elements.none { selector(it) == selector(t) } }

    companion object
}
