package com.avito.runner.scheduler.metrics

import com.avito.runner.scheduler.metrics.model.TestKey
import com.avito.runner.scheduler.metrics.model.TestTimestamps
import com.avito.runner.scheduler.metrics.model.createStubInstance
import com.avito.runner.scheduler.metrics.model.toTestKey
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class TestMetricsAggregatorTest {

    @Test
    fun `initial delay - is diff between suite start and first test start`() {
        val aggregator = createTestMetricsAggregator(
            testSuiteStartedTime = 10,
            testTimestamps = mapOf(
                "test1".toTestKey() to TestTimestamps.createStubInstance(started = 25),
                "test2".toTestKey() to TestTimestamps.createStubInstance(started = 35)
            )
        )

        val result = aggregator.initialDelay()

        assertThat(result).isEqualTo(15)
    }

    @Test
    fun `end delay - is diff between last test finish and suite finish`() {
        val aggregator = createTestMetricsAggregator(
            testSuiteEndedTime = 50,
            testTimestamps = mapOf(
                "test1".toTestKey() to TestTimestamps.createStubInstance(finished = 25),
                "test2".toTestKey() to TestTimestamps.createStubInstance(finished = 35)
            )
        )

        val result = aggregator.endDelay()

        assertThat(result).isEqualTo(15)
    }

    @Test
    fun `suite time - is diff between first test start and last test finish`() {
        val aggregator = createTestMetricsAggregator(
            testTimestamps = mapOf(
                "test1".toTestKey() to TestTimestamps.createStubInstance(started = 10, finished = 20),
                "test2".toTestKey() to TestTimestamps.createStubInstance(started = 15, finished = 45),
                "test3".toTestKey() to TestTimestamps.createStubInstance(started = 10, finished = 25),
                "test4".toTestKey() to TestTimestamps.createStubInstance(started = 30, finished = 35)
            )
        )

        val result = aggregator.suiteTime()

        assertThat(result).isEqualTo(35)
    }

    @Test
    fun `total time - is diff between suite started and suite finished`() {
        val aggregator = createTestMetricsAggregator(
            testSuiteStartedTime = 10,
            testSuiteEndedTime = 44
        )

        val result = aggregator.totalTime()

        assertThat(result).isEqualTo(34)
    }

    @Test
    fun `median queue time - is median value for all tests between suite start and test claimed a device`() {
        val aggregator = createTestMetricsAggregator(
            testSuiteStartedTime = 5,
            testTimestamps = mapOf(
                "test1".toTestKey() to TestTimestamps.createStubInstance(onDevice = 10),
                "test2".toTestKey() to TestTimestamps.createStubInstance(onDevice = 15),
                "test3".toTestKey() to TestTimestamps.createStubInstance(onDevice = 10),
                "test4".toTestKey() to TestTimestamps.createStubInstance(onDevice = 30)
            )
        )

        val result = aggregator.medianQueueTime()

        assertThat(result).isEqualTo(7) // median is 7.5, but rounded (ok for unix time)
    }

    @Test
    fun `median install time - is median value for all tests between device claim and test start`() {
        val aggregator = createTestMetricsAggregator(
            testTimestamps = mapOf(
                "test1".toTestKey() to TestTimestamps.createStubInstance(onDevice = 10, started = 13),
                "test2".toTestKey() to TestTimestamps.createStubInstance(onDevice = 15, started = 16),
                "test3".toTestKey() to TestTimestamps.createStubInstance(onDevice = 10, started = 19),
                "test4".toTestKey() to TestTimestamps.createStubInstance(onDevice = 30, started = 35)
            )
        )

        val result = aggregator.medianInstallationTime()

        assertThat(result).isEqualTo(4)
    }

    private fun createTestMetricsAggregator(
        testSuiteStartedTime: Long = 0,
        testSuiteEndedTime: Long = 0,
        testTimestamps: Map<TestKey, TestTimestamps> = emptyMap()
    ): TestMetricsAggregator {
        return TestMetricsAggregatorImpl(
            testSuiteStartedTime = testSuiteStartedTime,
            testSuiteEndedTime = testSuiteEndedTime,
            testTimestamps = testTimestamps
        )
    }
}
