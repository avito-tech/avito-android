package com.avito.runner.scheduler.metrics

import com.avito.math.fromZeroToHundredPercent
import com.avito.runner.scheduler.metrics.model.DeviceKey
import com.avito.runner.scheduler.metrics.model.DeviceWorkerEvent
import com.avito.runner.scheduler.metrics.model.TestExecutionEvent
import com.avito.runner.scheduler.metrics.model.createStubInstance
import com.avito.runner.scheduler.metrics.model.toDeviceKey
import com.avito.runner.scheduler.metrics.model.toTestKey
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

internal class TestRunnerMetricsProviderTest {

    @Test
    fun `initial delay - is diff between suite start and first test start`() {
        val aggregator = createTestMetricsAggregator(
            testSuiteStartedTime = Instant.ofEpochMilli(10),
            deviceWorkerEvents = mapOf(
                "12345".toDeviceKey() to DeviceWorkerEvent.createStubInstance(
                    testExecutionEvents = mutableMapOf(
                        "test1".toTestKey() to TestExecutionEvent.Finished(
                            intentionReceived = Instant.ofEpochMilli(10),
                            testStarted = Instant.ofEpochMilli(25),
                            finished = Instant.ofEpochMilli(30)
                        ),
                        "test2".toTestKey() to TestExecutionEvent.Finished(
                            intentionReceived = Instant.ofEpochMilli(10),
                            testStarted = Instant.ofEpochMilli(25),
                            finished = Instant.ofEpochMilli(30)
                        ),
                    )
                )
            )
        )

        val result = aggregator.initialDelay()

        assertThat(result.getOrThrow()).isEqualTo(Duration.ofMillis(15))
    }

    @Test
    fun `end delay - is diff between last test finish and suite finish`() {
        val aggregator = createTestMetricsAggregator(
            testSuiteEndedTime = Instant.ofEpochMilli(50),
            deviceWorkerEvents = mapOf(
                "12345".toDeviceKey() to DeviceWorkerEvent.createStubInstance(
                    testExecutionEvents = mutableMapOf(
                        "test1".toTestKey() to TestExecutionEvent.Finished(
                            intentionReceived = Instant.ofEpochMilli(10),
                            testStarted = Instant.ofEpochMilli(20),
                            finished = Instant.ofEpochMilli(25)
                        ),
                        "test2".toTestKey() to TestExecutionEvent.Finished(
                            intentionReceived = Instant.ofEpochMilli(10),
                            testStarted = Instant.ofEpochMilli(30),
                            finished = Instant.ofEpochMilli(35)
                        ),
                    )
                )
            )
        )

        val result = aggregator.endDelay()

        assertThat(result.getOrThrow()).isEqualTo(Duration.ofMillis(15))
    }

    @Test
    fun `suite time - is diff between first test start and last test finish`() {
        val aggregator = createTestMetricsAggregator(
            deviceWorkerEvents = mapOf(
                "12345".toDeviceKey() to DeviceWorkerEvent.createStubInstance(
                    testExecutionEvents = mutableMapOf(
                        "test1".toTestKey() to TestExecutionEvent.Finished(
                            intentionReceived = Instant.ofEpochMilli(10),
                            testStarted = Instant.ofEpochMilli(10),
                            finished = Instant.ofEpochMilli(20)
                        ),
                        "test2".toTestKey() to TestExecutionEvent.Finished(
                            intentionReceived = Instant.ofEpochMilli(10),
                            testStarted = Instant.ofEpochMilli(15),
                            finished = Instant.ofEpochMilli(45)
                        ),
                        "test3".toTestKey() to TestExecutionEvent.Finished(
                            intentionReceived = Instant.ofEpochMilli(10),
                            testStarted = Instant.ofEpochMilli(10),
                            finished = Instant.ofEpochMilli(25)
                        ),
                        "test4".toTestKey() to TestExecutionEvent.Finished(
                            intentionReceived = Instant.ofEpochMilli(10),
                            testStarted = Instant.ofEpochMilli(30),
                            finished = Instant.ofEpochMilli(35)
                        ),
                    )
                )
            )
        )

        val result = aggregator.suiteTime()

        assertThat(result.getOrThrow()).isEqualTo(Duration.ofMillis(35))
    }

    @Test
    fun `total time - is diff between suite started and suite finished`() {
        val aggregator = createTestMetricsAggregator(
            testSuiteStartedTime = Instant.ofEpochMilli(10),
            testSuiteEndedTime = Instant.ofEpochMilli(44)
        )

        val result = aggregator.totalTime()

        assertThat(result).isEqualTo(Duration.ofMillis(34))
    }

    @Test
    fun `median queue time - is median value for all tests between suite start and test claimed a device`() {
        val aggregator = createTestMetricsAggregator(
            testSuiteStartedTime = Instant.ofEpochMilli(5),
            deviceWorkerEvents = mapOf(
                "12345".toDeviceKey() to DeviceWorkerEvent.createStubInstance(
                    testExecutionEvents = mutableMapOf(
                        "test1".toTestKey() to TestExecutionEvent.Finished(
                            intentionReceived = Instant.ofEpochMilli(10),
                            testStarted = Instant.ofEpochMilli(40),
                            finished = Instant.ofEpochMilli(50)
                        ),
                        "test2".toTestKey() to TestExecutionEvent.Finished(
                            intentionReceived = Instant.ofEpochMilli(15),
                            testStarted = Instant.ofEpochMilli(40),
                            finished = Instant.ofEpochMilli(50)
                        ),
                        "test3".toTestKey() to TestExecutionEvent.Finished(
                            intentionReceived = Instant.ofEpochMilli(10),
                            testStarted = Instant.ofEpochMilli(40),
                            finished = Instant.ofEpochMilli(50)
                        ),
                        "test4".toTestKey() to TestExecutionEvent.Finished(
                            intentionReceived = Instant.ofEpochMilli(30),
                            testStarted = Instant.ofEpochMilli(40),
                            finished = Instant.ofEpochMilli(50)
                        ),
                    )
                )
            )
        )

        val result = aggregator.medianQueueTime()

        assertThat(result.getOrThrow()).isEqualTo(Duration.ofMillis(7)) // median is 7.5, but rounded (ok for unix time)
    }

    @Test
    fun `median install time - is median value for all tests between device claim and test start`() {
        val aggregator = createTestMetricsAggregator(
            deviceWorkerEvents = mapOf(
                "12345".toDeviceKey() to DeviceWorkerEvent.createStubInstance(
                    testExecutionEvents = mutableMapOf(
                        "test1".toTestKey() to TestExecutionEvent.Finished(
                            intentionReceived = Instant.ofEpochMilli(10),
                            testStarted = Instant.ofEpochMilli(13),
                            finished = Instant.ofEpochMilli(15)
                        ),
                        "test2".toTestKey() to TestExecutionEvent.Finished(
                            intentionReceived = Instant.ofEpochMilli(15),
                            testStarted = Instant.ofEpochMilli(16),
                            finished = Instant.ofEpochMilli(18)
                        ),
                        "test3".toTestKey() to TestExecutionEvent.Finished(
                            intentionReceived = Instant.ofEpochMilli(10),
                            testStarted = Instant.ofEpochMilli(19),
                            finished = Instant.ofEpochMilli(25)
                        ),
                        "test4".toTestKey() to TestExecutionEvent.Finished(
                            intentionReceived = Instant.ofEpochMilli(30),
                            testStarted = Instant.ofEpochMilli(35),
                            finished = Instant.ofEpochMilli(36)
                        ),
                    )
                )
            )
        )

        val result = aggregator.medianInstallationTime()

        assertThat(result.getOrThrow()).isEqualTo(Duration.ofMillis(4))
    }

    @Test
    fun `median device utilization - is median value for all valuable work to total work`() {
        val aggregator = createTestMetricsAggregator(
            deviceWorkerEvents = mapOf(
                "12345".toDeviceKey() to DeviceWorkerEvent.createStubInstance(
                    testExecutionEvents = mutableMapOf(
                        "test1".toTestKey() to TestExecutionEvent.Finished(
                            intentionReceived = Instant.ofEpochMilli(10),
                            testStarted = Instant.ofEpochMilli(10),
                            finished = Instant.ofEpochMilli(15)
                        ),
                        "test2".toTestKey() to TestExecutionEvent.Finished(
                            intentionReceived = Instant.ofEpochMilli(20),
                            testStarted = Instant.ofEpochMilli(20),
                            finished = Instant.ofEpochMilli(25)
                        ),
                        "test3".toTestKey() to TestExecutionEvent.Finished(
                            intentionReceived = Instant.ofEpochMilli(35),
                            testStarted = Instant.ofEpochMilli(35),
                            finished = Instant.ofEpochMilli(45)
                        ),
                    ),
                    finished = Instant.ofEpochMilli(50)
                )
            )
        )

        val result = aggregator.medianDeviceUtilization()

        assertThat(result.getOrThrow()).isEqualTo(40.fromZeroToHundredPercent())
    }

    private fun createTestMetricsAggregator(
        testSuiteStartedTime: Instant = Instant.ofEpochMilli(0),
        testSuiteEndedTime: Instant = Instant.ofEpochMilli(0),
        deviceWorkerEvents: Map<DeviceKey, DeviceWorkerEvent> = emptyMap()
    ): TestRunnerMetricsProvider {
        return TestRunnerMetricsProviderImpl(
            testSuiteStartedTime = testSuiteStartedTime,
            testSuiteEndedTime = testSuiteEndedTime,
            deviceWorkerEvents = deviceWorkerEvents
        )
    }
}
