package com.avito.instrumentation.internal.finalizer.verdict

import com.avito.report.model.AndroidTest
import com.avito.report.model.Flakiness
import com.avito.report.model.TestStaticData
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

        val notReportedTests = getLostTests(
            initialTestSuite = initialTestSuite,
            testResults = testResults
        )

        val unsuppressedFailedTests = mutableListOf<TestStaticData>()

        val intermediateResult = when {

            failedTests.isNotEmpty() -> when {
                suppressFailure -> Verdict.Success.Suppressed(
                    testResults = testResults,
                    failedTests = failedTests
                )

                suppressFlaky -> {
                    val (flaky, notFlaky) = failedTests.partition { it.flakiness is Flakiness.Flaky }

                    val hasFailedTestsNotMarkedAsFlaky = notFlaky.isNotEmpty()

                    unsuppressedFailedTests.addAll(notFlaky)

                    when {
                        hasFailedTestsNotMarkedAsFlaky -> Verdict.Failure(
                            testResults = testResults,
                            unsuppressedFailedTests = notFlaky.toSet(),
                            notReportedTests = notReportedTests
                        )

                        else -> Verdict.Success.Suppressed(
                            testResults = testResults,
                            failedTests = flaky.toSet()
                        )
                    }
                }
                else -> {
                    unsuppressedFailedTests.addAll(failedTests)
                    Verdict.Failure(
                        testResults = testResults,
                        unsuppressedFailedTests = failedTests,
                        notReportedTests = notReportedTests
                    )
                }
            }
            else -> Verdict.Success.OK(testResults = testResults)
        }

        return if (notReportedTests.isNotEmpty()) {
            Verdict.Failure(
                testResults = testResults,
                notReportedTests = notReportedTests,
                unsuppressedFailedTests = unsuppressedFailedTests
            )
        } else {
            intermediateResult
        }
    }

    private fun getFailedTests(testVerdicts: Collection<AndroidTest>): Set<TestStaticData> {
        return testVerdicts.filterIsInstance<AndroidTest.Completed>().filter { it.incident != null }.toSet()
    }

    private fun getLostTests(
        initialTestSuite: Set<TestStaticData>,
        testResults: Collection<AndroidTest>
    ): Set<AndroidTest.Lost> {
        val lostTests = testResults.filterIsInstance<AndroidTest.Lost>()

        val notReportedTests: List<AndroidTest.Lost> = initialTestSuite.subtract(testResults).map {
            AndroidTest.Lost.createWithoutInfo(
                testStaticData = it,
                currentTimeSec = timeProvider.nowInSeconds()
            )
        }

        return (lostTests + notReportedTests).toSet()
    }
}
