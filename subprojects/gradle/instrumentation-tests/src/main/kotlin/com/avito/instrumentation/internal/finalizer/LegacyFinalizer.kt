package com.avito.instrumentation.internal.finalizer

import com.avito.android.runner.report.LegacyReport
import com.avito.instrumentation.internal.InstrumentationTestsAction
import com.avito.instrumentation.internal.finalizer.action.FinalizeAction
import com.avito.instrumentation.internal.finalizer.verdict.HasFailedTestDeterminer
import com.avito.instrumentation.internal.finalizer.verdict.HasNotReportedTestsDeterminer
import com.avito.instrumentation.internal.finalizer.verdict.Verdict
import com.avito.instrumentation.internal.finalizer.verdict.VerdictDeterminer
import com.avito.instrumentation.internal.scheduling.TestsScheduler
import com.avito.utils.BuildFailer

internal class LegacyFinalizer(
    private val hasFailedTestDeterminer: HasFailedTestDeterminer,
    private val hasNotReportedTestsDeterminer: HasNotReportedTestsDeterminer,
    private val verdictDeterminer: VerdictDeterminer,
    private val actions: List<FinalizeAction>,
    private val buildFailer: BuildFailer,
    private val params: InstrumentationTestsAction.Params,
    private val report: LegacyReport
) : InstrumentationTestActionFinalizer {

    override fun finalize(testsExecutionResults: TestsScheduler.Result) {

        val reportedTestsResult = report.getTests(testsExecutionResults.testSuite.testsToRun.map { it.test.name })

        reportedTestsResult
            .onSuccess { tests ->

                val failedTests = hasFailedTestDeterminer.determine(runResult = tests)

                val notReportedTests = hasNotReportedTestsDeterminer.determine(
                    runResult = tests,
                    allTests = testsExecutionResults.testSuite.testsToRun.map { it.test }
                )

                val testRunResult = TestRunResult(
                    reportedTests = tests,
                    failed = failedTests,
                    notReported = notReportedTests
                )

                val verdict = verdictDeterminer.determine(
                    failed = failedTests,
                    notReported = notReportedTests
                )

                actions.forEach { it.action(testRunResult, verdict) }

                when (verdict) {
                    is Verdict.Success -> {
                        // empty
                    }
                    is Verdict.Failure ->
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
