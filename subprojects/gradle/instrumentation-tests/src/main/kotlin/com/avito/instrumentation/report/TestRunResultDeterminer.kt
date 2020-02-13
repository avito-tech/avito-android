package com.avito.instrumentation.report

import com.avito.instrumentation.TestRunResult
import com.avito.instrumentation.TestRunResult.Failure
import com.avito.instrumentation.TestRunResult.OK
import com.avito.instrumentation.TestRunResult.Suppressed
import com.avito.report.model.SimpleRunTest
import org.funktionale.tries.Try

interface TestRunResultDeterminer {

    fun determine(
        runResult: Try<List<SimpleRunTest>>,
        notReportedTests: HasNotReportedTestsDeterminer.Result
    ): TestRunResult
}

class TestRunResultDeterminerImplementation(
    private val suppressFailure: Boolean,
    private val suppressGroups: List<String>
) : TestRunResultDeterminer {

    override fun determine(
        runResult: Try<List<SimpleRunTest>>,
        notReportedTests: HasNotReportedTestsDeterminer.Result
    ): TestRunResult {

        when (notReportedTests) {
            is HasNotReportedTestsDeterminer.Result.FailedToDetermine -> {
                return Failure.InfrastructureFailure(
                    reason = "Failed to determine missed tests because: ${notReportedTests.exception}"
                )
            }
        }

        return runResult.fold(
            { testData ->
                val failedTests = testData.filter { !it.status.isSuccessful }
                val hasFailedTests = failedTests.isNotEmpty()

                when {
                    hasFailedTests -> {
                        when {
                            suppressFailure -> Suppressed(
                                testData = testData,
                                reason = "There are failed tests, but suppressed by configuration flag: 'suppressFailure'."
                            )
                            failedTests.all {
                                isSuppressedByGroup(it.groupList, suppressGroups)
                            } -> Suppressed(
                                testData = testData,
                                reason = "There are failed tests, but suppressed by groups config: $suppressGroups."
                            )
                            else -> {
                                val blockingTests = failedTests
                                    .filter { !isSuppressedByGroup(it.groupList, suppressGroups) }
                                    .joinToString(separator = "\n") { it.name }

                                Failure.ThereWereFailedTests(
                                    testData = testData,
                                    reason = "There are failed tests that blocks merge:\n$blockingTests"
                                )
                            }
                        }
                    }
                    else -> OK(testData = testData)
                }
            },
            { exception ->
                Failure.InfrastructureFailure(reason = "Can't get test data, because: ${exception.message}")
            }
        )
    }

    private fun isSuppressedByGroup(groupList: List<String>, suppressGroups: List<String>): Boolean {
        return groupList.intersect(suppressGroups).isNotEmpty()
    }
}
