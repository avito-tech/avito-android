package com.avito.runner.scheduler

import com.avito.runner.logging.Logger
import com.avito.runner.millisecondsToHumanReadableTime
import com.avito.runner.scheduler.report.Reporter
import com.avito.runner.scheduler.report.SummaryReportMaker
import com.avito.runner.scheduler.runner.TestRunner
import com.avito.runner.scheduler.runner.TestRunnerResult
import com.avito.runner.scheduler.runner.model.TestRunRequest
import kotlinx.coroutines.runBlocking

internal class Entrypoint(
    private val testRunner: TestRunner,
    private val summaryReportMaker: SummaryReportMaker,
    private val reporter: Reporter,
    private val logger: Logger
) {

    fun run(requests: List<TestRunRequest>) {
        val startTime = System.currentTimeMillis()

        val runResult: TestRunnerResult = runBlocking {
            testRunner.runTests(
                tests = requests
            )
        }

        val summary = summaryReportMaker.make(
            runResult = runResult,
            startTimeMilliseconds = startTime
        )

        reporter.report(summary)

        logger.log("Test run finished")
        logger.log(
            "Runs results: " +
                "passed = ${summary.successRunsCount}, " +
                "failed = ${summary.failedRunsCount}, " +
                "ignored = ${summary.ignoredRunsCount}, " +
                "took ${summary.durationMilliseconds.millisecondsToHumanReadableTime()}."
        )
        logger.log(
            "Matching results: " +
                "matched = ${summary.matchedCount}, " +
                "mismatched = ${summary.mismatched}, " +
                "ignored = ${summary.ignoredCount}."
        )
    }
}
