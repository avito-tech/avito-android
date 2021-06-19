package com.avito.runner.scheduler.runner

import com.avito.android.Result
import com.avito.android.runner.devices.DevicesProvider
import com.avito.android.runner.devices.model.ReservationData
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.model.TestStaticData
import com.avito.runner.millisecondsToHumanReadableTime
import com.avito.runner.reservation.DeviceReservationWatcher
import com.avito.runner.scheduler.metrics.TestMetricsListener
import com.avito.runner.scheduler.report.Reporter
import com.avito.runner.scheduler.report.SummaryReportMaker
import com.avito.runner.scheduler.runner.model.TargetGroup
import com.avito.runner.scheduler.runner.model.TestRunRequestFactory
import com.avito.runner.scheduler.runner.model.TestRunResult
import com.avito.runner.scheduler.runner.model.TestRunnerResult
import com.avito.runner.scheduler.runner.model.TestWithTarget
import com.avito.runner.scheduler.runner.scheduler.TestExecutionScheduler
import com.avito.runner.service.DeviceWorkerPool
import com.avito.runner.service.listener.TestListener
import com.avito.runner.service.model.TestCase
import kotlinx.coroutines.coroutineScope

internal class TestRunnerImpl(
    private val scheduler: TestExecutionScheduler,
    private val reservationWatcher: DeviceReservationWatcher,
    private val state: TestRunnerExecutionState,
    private val summaryReportMaker: SummaryReportMaker,
    private val reporter: Reporter,
    private val testMetricsListener: TestMetricsListener,
    private val devicesProvider: DevicesProvider,
    private val testRunRequestFactory: TestRunRequestFactory,
    private val testListenerProvider: (Map<TestCase, TestStaticData>) -> TestListener,
    loggerFactory: LoggerFactory
) : TestRunner {

    private val logger = loggerFactory.create<TestRunner>()

    override suspend fun runTests(tests: List<TestWithTarget>): Result<TestRunnerResult> {
        return coroutineScope {
            val startTime = System.currentTimeMillis()
            testMetricsListener.onTestSuiteStarted()
            logger.info("Test run started")
            val deviceWorkerPool: DeviceWorkerPool = devicesProvider.provideFor(
                reservations = getReservations(tests),
                testListener = testListenerProvider(testStaticDataByTestCase(tests)),
            )
            try {
                deviceWorkerPool.start()
                reservationWatcher.watch(state.deviceSignals)
                scheduler.start(
                    requests = getTestRequests(tests),
                )

                val expectedResultsCount = tests.count()

                val gottenResults = mutableListOf<TestRunResult>()
                for (result in state.results) {
                    gottenResults.add(result)
                    val gottenCount = gottenResults.size

                    logger.debug(
                        "Result for test: %s received after %d tries. Progress (%s)".format(
                            result.request.testCase.name,
                            result.result.size,
                            "$gottenCount/$expectedResultsCount"
                        )
                    )

                    if (gottenCount == expectedResultsCount) {
                        break
                    }
                }
                val result = TestRunnerResult(
                    runs = gottenResults.associate {
                        it.request to it.result
                    }
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
                logger.info("Test run end successfully")
                Result.Success(result)
            } catch (e: Throwable) {
                logger.critical("Test run end with error", e)
                Result.Failure(e)
            } finally {
                deviceWorkerPool.stop()
                state.cancel()
                devicesProvider.releaseDevices()
            }
        }
    }

    private fun getReservations(tests: List<TestWithTarget>): Collection<ReservationData> {
        val testsGroupedByTargets: Map<TargetGroup, List<TestWithTarget>> = tests.groupBy { test ->
            val target = test.target
            TargetGroup(target.name, target.reservation)
        }

        return testsGroupedByTargets
            .map { (target, tests) ->
                target.reservation.data(
                    tests = tests.map { it.test.name }
                )
            }
    }

    private fun testStaticDataByTestCase(
        testsToRun: List<TestWithTarget>
    ): Map<TestCase, TestStaticData> {
        return testsToRun.associate { testWithTarget ->
            TestCase(
                name = testWithTarget.test.name,
                deviceName = testWithTarget.target.deviceName
            ) to testWithTarget.test
        }
    }

    private fun getTestRequests(tests: List<TestWithTarget>) =
        tests.map { test -> testRunRequestFactory.create(test) }
}
