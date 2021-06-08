package com.avito.runner.scheduler.runner

import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.millisecondsToHumanReadableTime
import com.avito.runner.reservation.DeviceReservationWatcher
import com.avito.runner.scheduler.metrics.TestMetricsListener
import com.avito.runner.scheduler.report.Reporter
import com.avito.runner.scheduler.report.SummaryReportMaker
import com.avito.runner.scheduler.runner.model.TestRunRequest
import com.avito.runner.scheduler.runner.model.TestRunResult
import com.avito.runner.scheduler.runner.scheduler.TestExecutionScheduler
import com.avito.runner.service.DeviceWorkerPool
import kotlinx.coroutines.coroutineScope

internal class TestRunnerImpl(
    private val scheduler: TestExecutionScheduler,
    private val deviceWorkerPool: DeviceWorkerPool,
    private val reservationWatcher: DeviceReservationWatcher,
    private val state: TestRunnerExecutionState,
    private val summaryReportMaker: SummaryReportMaker,
    private val reporter: Reporter,
    private val testMetricsListener: TestMetricsListener,
    loggerFactory: LoggerFactory
) : TestRunner {

    private val logger = loggerFactory.create<TestRunner>()

    override suspend fun runTests(tests: List<TestRunRequest>): TestRunnerResult {
        return coroutineScope {
            val startTime = System.currentTimeMillis()
            testMetricsListener.onTestSuiteStarted()
            logger.debug("Test run started")
            deviceWorkerPool.start(this)
            reservationWatcher.watch(state.deviceSignals, this)
            scheduler.start(
                requests = tests,
                scope = this
            )

            val expectedResultsCount = tests.count()

            val gottenResults = mutableListOf<TestRunResult>()
            for (result in state.results) {
                gottenResults.add(result)
                val gottenCount = gottenResults.size

                logger.debug(
                    "Result for test: %s received after %d tries. Progress (%s)".format(
                        result.request.testCase.testName,
                        result.result.size,
                        "$gottenCount/$expectedResultsCount"
                    )
                )

                if (gottenCount == expectedResultsCount) {
                    break
                }
            }

            deviceWorkerPool.stop()
            state.cancel()

            val result = TestRunnerResult(
                runs = gottenResults
                    .map {
                        it.request to it.result
                    }
                    .toMap()
            )

            val summaryReport = summaryReportMaker.make(result, startTime)
            reporter.report(report = summaryReport)
            logger.debug(
                "Test run finished. The results: " +
                    "passed = ${summaryReport.successRunsCount}, " +
                    "failed = ${summaryReport.failedRunsCount}, " +
                    "ignored = ${summaryReport.ignoredRunsCount}, " +
                    "took ${summaryReport.durationMilliseconds.millisecondsToHumanReadableTime()}."
            )
            logger.debug(
                "Matching results: " +
                    "matched = ${summaryReport.matchedCount}, " +
                    "mismatched = ${summaryReport.mismatched}, " +
                    "ignored = ${summaryReport.ignoredCount}."
            )

            testMetricsListener.onTestSuiteFinished()
            result
        }
    }
}
