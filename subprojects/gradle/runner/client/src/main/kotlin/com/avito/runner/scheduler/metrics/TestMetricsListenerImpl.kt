package com.avito.runner.scheduler.metrics

import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.worker.device.Device
import com.avito.time.TimeProvider
import java.util.concurrent.ConcurrentHashMap

internal class TestMetricsListenerImpl(
    private val testMetricsSender: TestMetricsSender,
    private val timeProvider: TimeProvider,
    private val loggerFactory: LoggerFactory
) : TestMetricsListener {

    private val logger = loggerFactory.create<TestMetricsListener>()

    private val onDeviceMarks = ConcurrentHashMap<TestKey, Long>()

    private val startedMarks = ConcurrentHashMap<TestKey, Long>()

    private val finishedMarks = ConcurrentHashMap<TestKey, Long>()

    private var testSuiteStartedTime: Long = 0

    override fun onTestSuiteStarted() {
        testSuiteStartedTime = timeProvider.nowInMillis()
    }

    override fun onDevice(
        device: Device,
        test: TestCase,
        targetPackage: String,
        executionNumber: Int
    ) {
        val key = TestKey(test, executionNumber)
        onDeviceMarks[key] = timeProvider.nowInMillis()
    }

    override fun started(
        device: Device,
        targetPackage: String,
        test: TestCase,
        executionNumber: Int
    ) {
        val key = TestKey(test, executionNumber)
        startedMarks[key] = timeProvider.nowInMillis()
    }

    override fun finished(
        device: Device,
        test: TestCase,
        targetPackage: String,
        result: TestCaseRun.Result,
        durationMilliseconds: Long,
        executionNumber: Int
    ) {
        val key = TestKey(test, executionNumber)
        finishedMarks[key] = timeProvider.nowInMillis()
    }

    override fun onTestSuiteFinished() {
        val metricsComputer = TestMetricsComputer(
            loggerFactory = loggerFactory,
            testSuiteStartedTime = testSuiteStartedTime,
            testSuiteEndedTime = timeProvider.nowInMillis(),
            onDeviceMarks = onDeviceMarks,
            startedMarks = startedMarks,
            finishedMarks = finishedMarks
        )

        with(testMetricsSender) {
            metricsComputer.computeInitialDelay()
                ?.let { sendInitialDelay(it) }
                ?: logger.warn("Not sending initial delay, no data")

            metricsComputer.computeAverageTestQueueTime()
                ?.let { sendAverageTestQueueTime(it) }
                ?: logger.warn("Not sending average test queue time, no data")

            metricsComputer.computeAverageTestStartTime()
                ?.let { sendAverageTestStartTime(it) }
                ?: logger.warn("Not sending average test start time, no data")

            metricsComputer.computeEndDelay()
                ?.let { sendEndDelay(it) }
                ?: logger.warn("Not sending end delay, no data")

            metricsComputer.computeSuiteTime()
                ?.let { sendSuiteTime(it) }
                ?: logger.warn("Not sending suite time, no data")

            sendTotalTime(metricsComputer.computeTotalTime())
        }
    }
}
