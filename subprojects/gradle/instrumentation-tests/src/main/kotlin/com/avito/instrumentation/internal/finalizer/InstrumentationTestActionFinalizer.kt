package com.avito.instrumentation.internal.finalizer

import com.avito.instrumentation.internal.InstrumentationTestsAction
import com.avito.instrumentation.internal.TestRunResult
import com.avito.instrumentation.internal.report.HasFailedTestDeterminer
import com.avito.instrumentation.internal.report.HasNotReportedTestsDeterminer
import com.avito.instrumentation.internal.scheduling.TestsScheduler
import com.avito.utils.BuildFailer

internal interface InstrumentationTestActionFinalizer {

    fun finalize(
        testsExecutionResults: TestsScheduler.Result
    )

    interface FinalizeAction {

        fun action(testRunResult: TestRunResult)
    }

    class Impl(
        private val hasFailedTestDeterminer: HasFailedTestDeterminer,
        private val hasNotReportedTestsDeterminer: HasNotReportedTestsDeterminer,
        private val actions: List<FinalizeAction>,
        private val buildFailer: BuildFailer,
        private val params: InstrumentationTestsAction.Params,
    ) : InstrumentationTestActionFinalizer {
        override fun finalize(
            testsExecutionResults: TestsScheduler.Result
        ) {
            val testRunResult = TestRunResult(
                reportedTests = testsExecutionResults.testsResult.getOrElse { emptyList() },
                failed = hasFailedTestDeterminer.determine(
                    runResult = testsExecutionResults.testsResult
                ),
                notReported = hasNotReportedTestsDeterminer.determine(
                    runResult = testsExecutionResults.testsResult,
                    allTests = testsExecutionResults.testSuite.testsToRun.map { it.test }
                )
            )

            actions.forEach { it.action(testRunResult) }

            when (testRunResult.verdict) {
                is TestRunResult.Verdict.Success -> {
                    // empty
                }
                is TestRunResult.Verdict.Failure -> buildFailer.failBuild(
                    "Instrumentation task failed. Look at verdict in the file: ${params.verdictFile}"
                )
            }
        }
    }
}
