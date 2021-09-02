package com.avito.runner.scheduler.metrics

import com.avito.runner.scheduler.metrics.model.DeviceWorkerState
import com.avito.runner.scheduler.metrics.model.addCompletedTestExecution
import com.avito.runner.scheduler.metrics.model.createFinishedStubInstance
import com.avito.runner.scheduler.metrics.model.toDeviceKey
import com.avito.runner.scheduler.metrics.model.toTestKey
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

internal class TestRunnerMetricsProviderTest {

    @Test
    fun `initial delay - is diff between suite start and first test start`() {
        val states = setOf<DeviceWorkerState>(
            DeviceWorkerState.createFinishedStubInstance("12345".toDeviceKey()) {
                addCompletedTestExecution(
                    testKey = "test1".toTestKey(),
                    intentionReceived = Instant.ofEpochMilli(10),
                    started = Instant.ofEpochMilli(25),
                    completed = Instant.ofEpochMilli(30)
                )
                addCompletedTestExecution(
                    testKey = "test2".toTestKey(),
                    intentionReceived = Instant.ofEpochMilli(10),
                    started = Instant.ofEpochMilli(25),
                    completed = Instant.ofEpochMilli(30)
                )
            }
        )
        val aggregator = createTestMetricsAggregator(
            testSuiteStartedTime = Instant.ofEpochMilli(10),
            deviceWorkerStates = states
        )

        val result = aggregator.initialDelay()

        assertThat(result.getOrThrow()).isEqualTo(Duration.ofMillis(15))
    }

    @Test
    fun `end delay - is diff between last test finish and suite finish`() {
        val states = setOf<DeviceWorkerState>(
            DeviceWorkerState.createFinishedStubInstance("12345".toDeviceKey()) {
                addCompletedTestExecution(
                    testKey = "test1".toTestKey(),
                    intentionReceived = Instant.ofEpochMilli(10),
                    started = Instant.ofEpochMilli(20),
                    completed = Instant.ofEpochMilli(25)
                )
                addCompletedTestExecution(
                    testKey = "test2".toTestKey(),
                    intentionReceived = Instant.ofEpochMilli(10),
                    started = Instant.ofEpochMilli(30),
                    completed = Instant.ofEpochMilli(35)
                )
            }
        )
        val aggregator = createTestMetricsAggregator(
            testSuiteEndedTime = Instant.ofEpochMilli(50),
            deviceWorkerStates = states
        )

        val result = aggregator.endDelay()

        assertThat(result.getOrThrow()).isEqualTo(Duration.ofMillis(15))
    }

    @Test
    fun `suite time - is diff between first test start and last test finish`() {
        val states = setOf<DeviceWorkerState>(
            DeviceWorkerState.createFinishedStubInstance("12345".toDeviceKey()) {
                addCompletedTestExecution(
                    testKey = "test1".toTestKey(),
                    intentionReceived = Instant.ofEpochMilli(10),
                    started = Instant.ofEpochMilli(10),
                    completed = Instant.ofEpochMilli(20)
                )
                addCompletedTestExecution(
                    testKey = "test2".toTestKey(),
                    intentionReceived = Instant.ofEpochMilli(10),
                    started = Instant.ofEpochMilli(15),
                    completed = Instant.ofEpochMilli(45)
                )
                addCompletedTestExecution(
                    testKey = "test3".toTestKey(),
                    intentionReceived = Instant.ofEpochMilli(10),
                    started = Instant.ofEpochMilli(10),
                    completed = Instant.ofEpochMilli(25)
                )
                addCompletedTestExecution(
                    testKey = "test4".toTestKey(),
                    intentionReceived = Instant.ofEpochMilli(10),
                    started = Instant.ofEpochMilli(30),
                    completed = Instant.ofEpochMilli(35)
                )
            }
        )
        val aggregator = createTestMetricsAggregator(
            deviceWorkerStates = states
        )

        val result = aggregator.suiteTime()

        assertThat(result.getOrThrow()).isEqualTo(Duration.ofMillis(35))
    }

    @Test
    fun `total time - is diff between suite started and suite finished`() {
        val aggregator = createTestMetricsAggregator(
            testSuiteStartedTime = Instant.ofEpochMilli(10),
            testSuiteEndedTime = Instant.ofEpochMilli(44),
            deviceWorkerStates = emptySet()
        )

        val result = aggregator.totalTime()

        assertThat(result).isEqualTo(Duration.ofMillis(34))
    }

    @Test
    fun `median queue time - is median value for all tests between suite start and test claimed a device`() {
        val states = setOf<DeviceWorkerState>(
            DeviceWorkerState.createFinishedStubInstance("12345".toDeviceKey()) {
                addCompletedTestExecution(
                    testKey = "test1".toTestKey(),
                    intentionReceived = Instant.ofEpochMilli(10),
                    started = Instant.ofEpochMilli(40),
                    completed = Instant.ofEpochMilli(50)
                )
                addCompletedTestExecution(
                    testKey = "test2".toTestKey(),
                    intentionReceived = Instant.ofEpochMilli(15),
                    started = Instant.ofEpochMilli(40),
                    completed = Instant.ofEpochMilli(50)
                )
                addCompletedTestExecution(
                    testKey = "test3".toTestKey(),
                    intentionReceived = Instant.ofEpochMilli(10),
                    started = Instant.ofEpochMilli(40),
                    completed = Instant.ofEpochMilli(50)
                )
                addCompletedTestExecution(
                    testKey = "test4".toTestKey(),
                    intentionReceived = Instant.ofEpochMilli(30),
                    started = Instant.ofEpochMilli(40),
                    completed = Instant.ofEpochMilli(50)
                )
            }
        )
        val aggregator = createTestMetricsAggregator(
            testSuiteStartedTime = Instant.ofEpochMilli(5),
            deviceWorkerStates = states
        )

        val result = aggregator.medianQueueTime()

        assertThat(result.getOrThrow()).isEqualTo(Duration.ofMillis(7)) // median is 7.5, but rounded (ok for unix time)
    }

    @Test
    fun `median install time - is median value for all tests between device claim and test start`() {
        val states = setOf<DeviceWorkerState>(
            DeviceWorkerState.createFinishedStubInstance("12345".toDeviceKey()) {
                addCompletedTestExecution(
                    testKey = "test1".toTestKey(),
                    intentionReceived = Instant.ofEpochMilli(10),
                    started = Instant.ofEpochMilli(13),
                    completed = Instant.ofEpochMilli(15)
                )
                addCompletedTestExecution(
                    testKey = "test2".toTestKey(),
                    intentionReceived = Instant.ofEpochMilli(15),
                    started = Instant.ofEpochMilli(16),
                    completed = Instant.ofEpochMilli(18)
                )
                addCompletedTestExecution(
                    testKey = "test3".toTestKey(),
                    intentionReceived = Instant.ofEpochMilli(10),
                    started = Instant.ofEpochMilli(19),
                    completed = Instant.ofEpochMilli(25)
                )
                addCompletedTestExecution(
                    testKey = "test4".toTestKey(),
                    intentionReceived = Instant.ofEpochMilli(30),
                    started = Instant.ofEpochMilli(35),
                    completed = Instant.ofEpochMilli(36)
                )
            }
        )
        val aggregator = createTestMetricsAggregator(
            deviceWorkerStates = states
        )

        val result = aggregator.medianInstallationTime()

        assertThat(result.getOrThrow()).isEqualTo(Duration.ofMillis(4))
    }

    @Test
    fun `two devices - devices living time - is sum of devices living`() {
        val states = setOf<DeviceWorkerState>(
            DeviceWorkerState.createFinishedStubInstance(
                deviceKey = "1".toDeviceKey(),
                finished = Instant.ofEpochMilli(30),
            ),
            DeviceWorkerState.createFinishedStubInstance(
                deviceKey = "2".toDeviceKey(),
                finished = Instant.ofEpochMilli(30),
            )
        )
        val aggregator = createTestMetricsAggregator(
            deviceWorkerStates = states
        )

        val result = aggregator.devicesLiving()

        assertThat(result).isEqualTo(Duration.ofMillis(60))
    }

    @Test
    fun `two devices - devices working time - sum of devices working`() {
        val states = setOf<DeviceWorkerState>(
            DeviceWorkerState.createFinishedStubInstance(
                deviceKey = "1".toDeviceKey(),
                finished = Instant.ofEpochMilli(30),
            ) {
                addCompletedTestExecution(
                    testKey = "test1".toTestKey(),
                    intentionReceived = Instant.ofEpochMilli(10),
                    started = Instant.ofEpochMilli(10),
                    completed = Instant.ofEpochMilli(15)
                )
            },
            DeviceWorkerState.createFinishedStubInstance(
                deviceKey = "2".toDeviceKey(),
                finished = Instant.ofEpochMilli(40),
            ) {
                addCompletedTestExecution(
                    testKey = "test1".toTestKey(),
                    intentionReceived = Instant.ofEpochMilli(10),
                    started = Instant.ofEpochMilli(10),
                    completed = Instant.ofEpochMilli(15)
                )
            }
        )
        val aggregator = createTestMetricsAggregator(
            deviceWorkerStates = states
        )

        val result = aggregator.devicesWorking()

        assertThat(result).isEqualTo(Duration.ofMillis(10))
    }

    @Test
    fun `two devices - devices idle time - is sum of idle`() {
        val states = setOf<DeviceWorkerState>(
            DeviceWorkerState.createFinishedStubInstance(
                deviceKey = "1".toDeviceKey(),
                finished = Instant.ofEpochMilli(30),
            ),
            DeviceWorkerState.createFinishedStubInstance(
                deviceKey = "2".toDeviceKey(),
                finished = Instant.ofEpochMilli(40),
            )
        )
        val aggregator = createTestMetricsAggregator(
            deviceWorkerStates = states
        )

        val result = aggregator.devicesIdle()

        assertThat(result).isEqualTo(Duration.ofMillis(70))
    }

    private fun createTestMetricsAggregator(
        testSuiteStartedTime: Instant = Instant.ofEpochMilli(0),
        testSuiteEndedTime: Instant = testSuiteStartedTime,
        deviceWorkerStates: Set<DeviceWorkerState>
    ): TestRunnerMetricsProvider {
        return TestRunnerMetricsProviderImpl(
            testSuiteStartedTime = testSuiteStartedTime,
            testSuiteEndedTime = testSuiteEndedTime,
            deviceWorkerStates = deviceWorkerStates
        )
    }
}
