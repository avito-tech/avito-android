package com.avito.runner.scheduler.metrics

import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.scheduler.metrics.model.TestKey
import com.avito.runner.scheduler.metrics.model.TestTimestamps
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.worker.device.Device
import com.avito.time.TimeProvider
import java.util.concurrent.ConcurrentHashMap

internal class TestMetricsListenerImpl(
    private val testMetricsSender: TestMetricsSender,
    private val timeProvider: TimeProvider,
    loggerFactory: LoggerFactory
) : TestMetricsListener {

    private val logger = loggerFactory.create<TestMetricsListener>()

    private val testTimestamps = ConcurrentHashMap<TestKey, TestTimestamps>()

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
        testTimestamps.compute(key) { _, oldValue ->
            oldValue.createOrUpdate { it.copy(onDevice = timeProvider.nowInMillis()) }
        }
    }

    override fun started(
        device: Device,
        targetPackage: String,
        test: TestCase,
        executionNumber: Int
    ) {
        val key = TestKey(test, executionNumber)
        testTimestamps.compute(key) { _, oldValue ->
            oldValue.createOrUpdate { it.copy(started = timeProvider.nowInMillis()) }
        }
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
        testTimestamps.compute(key) { _, oldValue ->
            oldValue.createOrUpdate { it.copy(finished = timeProvider.nowInMillis()) }
        }
    }

    override fun onTestSuiteFinished() {
        val aggregator: TestMetricsAggregator = createTestMetricsAggregator()

        with(testMetricsSender) {
            aggregator.initialDelay()
                ?.let { sendInitialDelay(it) }
                ?: logger.warn("Not sending initial delay, no data")

            aggregator.medianQueueTime()
                ?.let { sendMedianQueueTime(it) }
                ?: logger.warn("Not sending average test queue time, no data")

            aggregator.medianInstallationTime()
                ?.let { sendMedianInstallationTime(it) }
                ?: logger.warn("Not sending average test start time, no data")

            aggregator.endDelay()
                ?.let { sendEndDelay(it) }
                ?: logger.warn("Not sending end delay, no data")

            aggregator.suiteTime()
                ?.let { sendSuiteTime(it) }
                ?: logger.warn("Not sending suite time, no data")

            sendTotalTime(aggregator.totalTime())
        }
    }

    private fun createTestMetricsAggregator(): TestMetricsAggregator {
        return TestMetricsAggregatorImpl(
            testSuiteStartedTime = testSuiteStartedTime,
            testSuiteEndedTime = timeProvider.nowInMillis(),
            testTimestamps = testTimestamps
        )
    }

    private fun TestTimestamps?.createOrUpdate(updateFun: (TestTimestamps) -> TestTimestamps): TestTimestamps {
        return updateFun(this ?: TestTimestamps(0, 0, 0))
    }
}
