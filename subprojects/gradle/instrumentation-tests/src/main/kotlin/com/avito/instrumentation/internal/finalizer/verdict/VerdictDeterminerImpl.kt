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

        val lostTests = getLostTests(
            initialTestSuite = initialTestSuite,
            testResults = testResults
        )

        val failedTests = getFailedTests(testResults)

        val hasFailedTests = failedTests.isNotEmpty()

        val intermediateResult = when {
            hasFailedTests -> when {
                suppressFailure -> Verdict.Success.Suppressed(
                    testResults = testResults,
                    failedTests = failedTests
                )

                suppressFlaky -> {
                    val (flaky, notFlaky) = failedTests.partition { it.flakiness is Flakiness.Flaky }

                    val hasFailedTestsNotMarkedAsFlaky = notFlaky.isNotEmpty()

                    when {
                        hasFailedTestsNotMarkedAsFlaky -> Verdict.Failure(
                            testResults = testResults,
                            failedTests = notFlaky.toSet(),
                            lostTests = lostTests
                        )

                        else -> Verdict.Success.Suppressed(
                            testResults = testResults,
                            failedTests = flaky.toSet()
                        )
                    }
                }
                else -> Verdict.Failure(
                    testResults = testResults,
                    failedTests = failedTests,
                    lostTests = lostTests
                )
            }
            else -> Verdict.Success.OK(testResults = testResults)
        }

        val hasLostTests = lostTests.isNotEmpty()

        return if (hasLostTests) {
            Verdict.Failure(
                testResults = testResults,
                lostTests = lostTests,
                failedTests = failedTests
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
