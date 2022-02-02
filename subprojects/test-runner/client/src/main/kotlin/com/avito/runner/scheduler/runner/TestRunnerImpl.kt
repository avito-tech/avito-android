package com.avito.runner.scheduler.runner

import com.avito.android.Result
import com.avito.android.runner.devices.DevicesProvider
import com.avito.android.runner.devices.model.ReservationData
import com.avito.coroutines.extensions.withTimeoutOrDefault
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.config.TargetConfigurationData
import com.avito.runner.reservation.DeviceReservationWatcher
import com.avito.runner.scheduler.metrics.TestSuiteListener
import com.avito.runner.scheduler.report.Reporter
import com.avito.runner.scheduler.report.SummaryReportMaker
import com.avito.runner.scheduler.runner.model.TestRunRequestFactory
import com.avito.runner.scheduler.runner.model.TestRunResult
import com.avito.runner.scheduler.runner.model.TestRunnerResult
import com.avito.runner.scheduler.runner.scheduler.TestExecutionScheduler
import com.avito.runner.service.DeviceWorkerPool
import com.avito.runner.service.worker.device.Device
import com.avito.test.model.DeviceName
import com.avito.test.model.TestCase
import com.avito.time.millisecondsToHumanReadableTime
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.coroutineScope
import java.time.Duration

internal class TestRunnerImpl(
    private val scheduler: TestExecutionScheduler,
    private val reservationWatcher: DeviceReservationWatcher,
    private val state: TestRunnerExecutionState,
    private val summaryReportMaker: SummaryReportMaker,
    private val reporter: Reporter,
    private val testSuiteListener: TestSuiteListener,
    private val devicesProvider: DevicesProvider,
    private val testRunRequestFactory: TestRunRequestFactory,
    private val targets: List<TargetConfigurationData>,
    private val executionTimeout: Duration,
    loggerFactory: LoggerFactory
) : TestRunner {

    private val logger = loggerFactory.create<TestRunner>()

    override suspend fun runTests(tests: List<TestCase>): Result<TestRunnerResult> {
        return withTimeoutOrDefault(
            timeMillis = executionTimeout.toMillis(),
            default = Result.Failure(
                IllegalStateException(
                    "Test run finished with timeout after ${
                        executionTimeout.toMillis().millisecondsToHumanReadableTime()
                    }"
                )
            )
        ) {
            coroutineScope {
                val startTime = System.currentTimeMillis()
                testSuiteListener.onTestSuiteStarted()
                logger.info("Test run started")
                val deviceWorkerPool: DeviceWorkerPool = devicesProvider.provideFor(
                    reservations = getReservations(tests),
                )
                try {
                    deviceWorkerPool.start()
                    reservationWatcher.watch(state.deviceSignals)
                    scheduler.start(
                        requests = getTestRequests(tests),
                    )

                    val expectedResultsCount = tests.count()

                    val gottenResultsByDeviceName : MutableMap<String, MutableList<TestRunResult>> = mutableMapOf()
                    val expectedResultsCountByDeviceName = tests
                        .groupBy { it.deviceName.name }
                        .map { (deviceName, tests) ->
                            gottenResultsByDeviceName[deviceName] = mutableListOf()
                            deviceName to tests.count()
                        }

                    val gottenResults = mutableListOf<TestRunResult>()
                    for (result in state.results) {
                        gottenResults.add(result)
                        gottenResultsByDeviceName[result.request.testCase.deviceName.name]?.add(result)

                        expectedResultsCountByDeviceName.forEach { (deviceName, expectedCount) ->
                            val actualCount = gottenResultsByDeviceName[deviceName]?.size ?: 0
                            if (actualCount >= expectedCount) {
                                result.result
                                state.deviceSignals.send(Device.Signal.ReservationNotNeeded(deviceName))
                            }
                        }

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
                            it.request.testCase to it.result
                        }
                    )

                    val summaryReport = summaryReportMaker.make(gottenResults, startTime)
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
                            "mismatched = ${summaryReport.mismatchedCount}, " +
                            "ignored = ${summaryReport.ignoredCount}."
                    )

                    testSuiteListener.onTestSuiteFinished()
                    logger.info("Test run finished successfully")
                    Result.Success(result)
                } catch (e: Throwable) {
                    when (e) {
                        is TimeoutCancellationException -> logger.critical("Test run finished with timeout", e)
                        else -> logger.critical("Test run finished with error", e)
                    }
                    Result.Failure(e)
                } finally {
                    deviceWorkerPool.stop()
                    state.cancel()
                    devicesProvider.releaseDevices()
                }
            }
        }
    }

    private fun getReservations(tests: List<TestCase>): Collection<ReservationData> {
        val testsGroupedByDevice: Map<DeviceName, List<TestCase>> = tests.groupBy { test ->
            test.deviceName
        }

        return testsGroupedByDevice
            .map { (deviceName, tests) ->
                val target = requireNotNull(targets.firstOrNull { it.deviceName == deviceName }) {
                    "Can't find target $deviceName"
                }
                target.reservation.data(tests.size)
            }
    }

    private fun getTestRequests(tests: List<TestCase>) =
        tests.map { test -> testRunRequestFactory.create(test) }
}
