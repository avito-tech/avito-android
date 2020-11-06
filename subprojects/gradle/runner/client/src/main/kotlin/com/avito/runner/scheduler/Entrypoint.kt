package com.avito.runner.scheduler

import com.avito.logger.Logger
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

        log("Test run started. requests = $requests")

        val runResult: TestRunnerResult = runBlocking {
            testRunner.runTests(
                tests = requests,
                scope = this
            )
        }

        val summary = summaryReportMaker.make(
            runResult = runResult,
            startTimeMilliseconds = startTime
        )

        reporter.report(summary)

        log(
            "Test run finished. The results: " +
                "passed = ${summary.successRunsCount}, " +
                "failed = ${summary.failedRunsCount}, " +
                "ignored = ${summary.ignoredRunsCount}, " +
                "took ${summary.durationMilliseconds.millisecondsToHumanReadableTime()}."
        )
        log(
            "Matching results: " +
                "matched = ${summary.matchedCount}, " +
                "mismatched = ${summary.mismatched}, " +
                "ignored = ${summary.ignoredCount}."
        )
    }

    private fun log(message: String) {
        logger.debug("Entrypoint: $message")
    }
}
