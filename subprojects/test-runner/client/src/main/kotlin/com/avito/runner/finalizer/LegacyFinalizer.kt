package com.avito.runner.finalizer

import com.avito.android.runner.report.LegacyReport
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.model.TestName
import com.avito.runner.config.InstrumentationTestsActionParams
import com.avito.runner.finalizer.action.LegacyFinalizeAction
import com.avito.runner.finalizer.verdict.HasFailedTestDeterminer
import com.avito.runner.finalizer.verdict.HasNotReportedTestsDeterminer
import com.avito.runner.finalizer.verdict.LegacyVerdict
import com.avito.runner.finalizer.verdict.LegacyVerdictDeterminer
import com.avito.runner.scheduler.runner.model.TestSchedulerResult

internal class LegacyFinalizer(
    private val hasFailedTestDeterminer: HasFailedTestDeterminer,
    private val hasNotReportedTestsDeterminer: HasNotReportedTestsDeterminer,
    private val legacyVerdictDeterminer: LegacyVerdictDeterminer,
    private val actions: List<LegacyFinalizeAction>,
    private val params: InstrumentationTestsActionParams,
    loggerFactory: LoggerFactory,
    private val report: LegacyReport,
) : Finalizer {

    private val logger = loggerFactory.create<LegacyFinalizer>()

    override fun finalize(testSchedulerResults: TestSchedulerResult): Finalizer.Result {

        val testResults = report.getTests()
            .map { testsFromReport ->
                val testsToRun = testSchedulerResults.testsToRun.map { it.name }
                testsFromReport.filter { TestName(it.className, it.methodName) in testsToRun }
            }
            .onFailure { throwable ->
                logger.critical("Can't get test results", throwable)
            }
            .getOrElse { emptyList() }

        val failedTests = hasFailedTestDeterminer.determine(runResult = testResults)

        val notReportedTests = hasNotReportedTestsDeterminer.determine(
            runResult = testResults,
            allTests = testSchedulerResults.testsToRun
        )

        val testRunResult = TestRunResult(
            reportedTests = testResults,
            failed = failedTests,
            notReported = notReportedTests
        )

        val verdict = legacyVerdictDeterminer.determine(
            failed = failedTests,
            notReported = notReportedTests
        )

        actions.forEach { it.action(testRunResult, verdict) }

        return when (verdict) {
            is LegacyVerdict.Success ->
                Finalizer.Result.Ok

            is LegacyVerdict.Failure ->
                Finalizer.Result.Failure(
                    "Instrumentation task failed. Look at verdict in the file: ${params.verdictFile}"
                )
        }
    }
}
