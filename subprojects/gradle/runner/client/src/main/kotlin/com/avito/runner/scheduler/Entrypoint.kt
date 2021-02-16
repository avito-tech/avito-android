package com.avito.runner.scheduler

import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.millisecondsToHumanReadableTime
import com.avito.runner.scheduler.report.Reporter
import com.avito.runner.scheduler.report.SummaryReportMaker
import com.avito.runner.scheduler.runner.TestRunner
import com.avito.runner.scheduler.runner.TestRunnerResult
import com.avito.runner.scheduler.runner.model.TestRunRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking

internal class Entrypoint(
    private val testRunner: TestRunner,
    private val summaryReportMaker: SummaryReportMaker,
    private val reporter: Reporter,
    private val scope: CoroutineScope,
    loggerFactory: LoggerFactory
) {

    private val logger = loggerFactory.create<Entrypoint>()

    fun run(requests: List<TestRunRequest>) {
        val startTime = System.currentTimeMillis()

        logger.debug("Test run started. requests = $requests")

        val runResult: TestRunnerResult = if (requests.isNotEmpty()) {
            runBlocking {
                testRunner.runTests(
                    tests = requests,
                    scope = scope
                )
            }
        } else {
            TestRunnerResult(emptyMap())
        }

        val summary = summaryReportMaker.make(
            runResult = runResult,
            startTimeMilliseconds = startTime
        )

        reporter.report(summary)

        logger.debug(
            "Test run finished. The results: " +
                "passed = ${summary.successRunsCount}, " +
                "failed = ${summary.failedRunsCount}, " +
                "ignored = ${summary.ignoredRunsCount}, " +
                "took ${summary.durationMilliseconds.millisecondsToHumanReadableTime()}."
        )
        logger.debug(
            "Matching results: " +
                "matched = ${summary.matchedCount}, " +
                "mismatched = ${summary.mismatched}, " +
                "ignored = ${summary.ignoredCount}."
        )
    }

    companion object
}
