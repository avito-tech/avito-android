package com.avito.instrumentation.internal.finalizer

import com.avito.android.runner.report.LegacyReport
import com.avito.instrumentation.internal.InstrumentationTestsAction
import com.avito.instrumentation.internal.TestRunResult
import com.avito.instrumentation.internal.scheduling.TestsScheduler
import com.avito.utils.BuildFailer

internal class LegacyFinalizer(
    private val hasFailedTestDeterminer: HasFailedTestDeterminer,
    private val hasNotReportedTestsDeterminer: HasNotReportedTestsDeterminer,
    private val actions: List<InstrumentationTestActionFinalizer.FinalizeAction>,
    private val buildFailer: BuildFailer,
    private val params: InstrumentationTestsAction.Params,
    private val report: LegacyReport
) : InstrumentationTestActionFinalizer {

    override fun finalize(testsExecutionResults: TestsScheduler.Result) {

        val reportedTestsResult = report.getTests(testsExecutionResults.testSuite.testsToRun.map { it.test.name })

        reportedTestsResult
            .onSuccess { tests ->
                val testRunResult = TestRunResult(
                    reportedTests = tests,
                    failed = hasFailedTestDeterminer.determine(runResult = tests),
                    notReported = hasNotReportedTestsDeterminer.determine(
                        runResult = tests,
                        allTests = testsExecutionResults.testSuite.testsToRun.map { it.test }
                    )
                )

                actions.forEach { it.action(testRunResult) }

                when (testRunResult.verdict) {
                    is TestRunResult.Verdict.Success -> {
                        // empty
                    }
                    is TestRunResult.Verdict.Failure ->
                        buildFailer.failBuild(
                            "Instrumentation task failed. Look at verdict in the file: ${params.verdictFile}"
                        )
                }
            }
            .onFailure { throwable ->
                buildFailer.failBuild("Instrumentation task failed. Can't get test results", throwable)
            }
    }
}
