package com.avito.instrumentation.internal.finalizer

import com.avito.android.runner.report.LegacyReport
import com.avito.instrumentation.internal.finalizer.action.LegacyFinalizeAction
import com.avito.instrumentation.internal.finalizer.verdict.HasFailedTestDeterminer
import com.avito.instrumentation.internal.finalizer.verdict.HasNotReportedTestsDeterminer
import com.avito.instrumentation.internal.finalizer.verdict.LegacyVerdict
import com.avito.instrumentation.internal.finalizer.verdict.LegacyVerdictDeterminer
import com.avito.instrumentation.internal.scheduling.TestsScheduler
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.utils.BuildFailer
import java.io.File

internal class LegacyFinalizer(
    private val hasFailedTestDeterminer: HasFailedTestDeterminer,
    private val hasNotReportedTestsDeterminer: HasNotReportedTestsDeterminer,
    private val legacyVerdictDeterminer: LegacyVerdictDeterminer,
    private val actions: List<LegacyFinalizeAction>,
    private val buildFailer: BuildFailer,
    private val verdictFile: File,
    loggerFactory: LoggerFactory,
    private val report: LegacyReport,
) : InstrumentationTestActionFinalizer {

    private val logger = loggerFactory.create<LegacyFinalizer>()

    override fun finalize(testSchedulerResults: TestsScheduler.Result) {

        val fetchedTestResults =
            report.getTests(initialSuiteFilter = testSchedulerResults.initialTestSuite.map { it.name })
                .onFailure { throwable -> logger.critical("Can't get test results", throwable) }
                .getOrElse { emptyList() }

        val failedTests = hasFailedTestDeterminer.determine(runResult = fetchedTestResults)

        val notReportedTests = hasNotReportedTestsDeterminer.determine(
            runResult = fetchedTestResults,
            allTests = testSchedulerResults.initialTestSuite.toList()
        )

        val testRunResult = TestRunResult(
            reportedTests = fetchedTestResults,
            failed = failedTests,
            notReported = notReportedTests
        )

        val verdict = legacyVerdictDeterminer.determine(
            failed = failedTests,
            notReported = notReportedTests
        )

        actions.forEach { it.action(testRunResult, verdict) }

        when (verdict) {
            is LegacyVerdict.Success -> {
                // do nothing
            }
            is LegacyVerdict.Failure ->
                buildFailer.failBuild("Instrumentation task failed. Look at verdict in the file: $verdictFile")
        }
    }
}
