package com.avito.runner.scheduler.metrics

import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.scheduler.metrics.model.DeviceWorkerState
import com.avito.runner.scheduler.metrics.model.TestKey
import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.intention.Intention
import com.avito.runner.service.model.intention.State
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.listener.DeviceListener
import com.avito.runner.service.worker.model.DeviceInstallation
import com.avito.time.TimeProvider
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

private typealias DeviceWorkerStates = ConcurrentHashMap.KeySetView<DeviceWorkerState, Boolean>

internal class TestRunnerMetricsListener(
    private val testMetricsSender: TestRunnerMetricsSender,
    private val timeProvider: TimeProvider,
    loggerFactory: LoggerFactory
) : TestSuiteListener, DeviceListener {

    private val logger = loggerFactory.create<TestRunnerMetricsListener>()

    private val deviceWorkerStates: DeviceWorkerStates = ConcurrentHashMap.newKeySet()

    private var testSuiteStartedTime: Instant = Instant.now()

    override fun onTestSuiteStarted() {
        testSuiteStartedTime = timeProvider.nowInstant()
    }

    override suspend fun onDeviceCreated(device: Device, state: State) {
        check(
            deviceWorkerStates.add(
                DeviceWorkerState.Created(
                    created = timeProvider.nowInstant(),
                    testExecutionStates = ConcurrentHashMap.newKeySet(),
                    key = device.coordinate
                )
            )
        ) {
            "Device ${device.coordinate} already called onDeviceCreated"
        }
    }

    override suspend fun onIntentionReceived(device: Device, intention: Intention) {
        val key = device.coordinate
        val state = checkNotNull(deviceWorkerStates.singleOrNull { it.key == key }) {
            "Can't find DeviceWorkerState for $key"
        }
        state.testIntentionReceived(intention.testKey(), timeProvider.nowInstant())
    }

    override suspend fun onStatePrepared(device: Device, state: State) {
        // empty
    }

    override suspend fun onApplicationInstalled(device: Device, installation: DeviceInstallation) {
        // empty
    }

    override suspend fun onTestStarted(device: Device, intention: Intention) {
        val key = device.coordinate
        val state = checkNotNull(deviceWorkerStates.singleOrNull { it.key == key }) {
            "Can't find DeviceWorkerState for $key"
        }
        state.testStarted(intention.testKey(), timeProvider.nowInstant())
    }

    override suspend fun onTestCompleted(device: Device, intention: Intention, result: DeviceTestCaseRun) {
        val key = device.coordinate
        val state = checkNotNull(deviceWorkerStates.singleOrNull { it.key == key }) {
            "Can't find DeviceWorkerState for $key"
        }
        state.testCompleted(intention.testKey(), timeProvider.nowInstant())
    }

    override suspend fun onIntentionFail(device: Device, intention: Intention, reason: Throwable) {
        val key = device.coordinate
        val state = checkNotNull(deviceWorkerStates.singleOrNull { it.key == key }) {
            "Can't find DeviceWorkerState for $key"
        }
        state.testIntentionFailed(intention.testKey())
    }

    override suspend fun onDeviceDied(device: Device, message: String, reason: Throwable) {
        // alternative to onFinished terminated state
    }

    override suspend fun onFinished(device: Device) {
        val key = device.coordinate
        val state = checkNotNull(deviceWorkerStates.singleOrNull { it.key == key }) {
            "Can't find DeviceWorkerState for $key"
        }
        val newState = state.finish(timeProvider.nowInstant())
        deviceWorkerStates.replace(state, newState)
    }

    private fun DeviceWorkerStates.replace(
        old: DeviceWorkerState,
        new: DeviceWorkerState
    ) {
        remove(old)
        add(new)
    }

    override fun onTestSuiteFinished() {
        val provider = createMetricsProvider()

        with(testMetricsSender) {
            provider.initialDelay().fold(
                { sendInitialDelay(it) },
                { logger.warn("Not sending initial delay, no data") }
            )

            provider.medianQueueTime().fold(
                { sendMedianQueueTime(it) },
                { logger.warn("Not sending median test queue time, no data") }
            )
            provider.medianInstallationTime().fold(
                { sendMedianInstallationTime(it) },
                { logger.warn("Not sending median test start time, no data") }
            )

            provider.endDelay().fold(
                { sendEndDelay(it) },
                { logger.warn("Not sending end delay, no data") }
            )

            provider.suiteTime().fold(
                { sendSuiteTime(it) },
                { logger.warn("Not sending suite time, no data") }
            )

            sendTotalTime(provider.totalTime())
            sendDevicesLiving(provider.devicesLiving())
            sendDevicesWorking(provider.devicesWorking())
            sendDevicesIdle(provider.devicesIdle())
        }
    }

    private fun Intention.testKey() = TestKey(action.test, action.executionNumber)

    private fun createMetricsProvider(): TestRunnerMetricsProvider {
        return TestRunnerMetricsProviderImpl(
            testSuiteStartedTime = testSuiteStartedTime,
            testSuiteEndedTime = timeProvider.nowInstant(),
            deviceWorkerStates = deviceWorkerStates
        )
    }
}
