package com.avito.runner.scheduler.metrics

import com.avito.android.Result
import com.avito.math.Percent
import com.avito.math.fromZeroToHundredPercent
import com.avito.math.median
import com.avito.runner.scheduler.metrics.model.DeviceKey
import com.avito.runner.scheduler.metrics.model.DeviceWorkerEvent
import com.avito.runner.scheduler.metrics.model.TestExecutionEvent
import java.time.Duration
import java.time.Instant

internal data class TestRunnerMetricsProviderImpl(
    private val testSuiteStartedTime: Instant,
    private val testSuiteEndedTime: Instant,
    private val deviceWorkerEvents: Map<DeviceKey, DeviceWorkerEvent>
) : TestRunnerMetricsProvider {

    private val testTimestamps = deviceWorkerEvents.flatMap { it.value.testExecutionEvents.values }

    private val firstTestStarted: Result<Instant> = testTimestamps.filterIsInstance<TestExecutionEvent.Finished>()
        .map { it.testStarted }
        .minOrNull()
        .toResult { "Cannot calculate first started test time" }

    private val lastTestEnded: Result<Instant> = testTimestamps.filterIsInstance<TestExecutionEvent.Finished>()
        .map { it.finished }
        .maxOrNull()
        .toResult { "Cannot calculate last ended test time" }

    private val queueTimes: List<Duration> =
        testTimestamps
            .filterIsInstance<TestExecutionEvent.Finished>()
            .map { Duration.between(testSuiteStartedTime, it.intentionReceived) }

    private val installationTimes: List<Duration> =
        testTimestamps
            .filterIsInstance<TestExecutionEvent.Finished>()
            .map { it.installationTime }

    override fun initialDelay(): Result<Duration> = firstTestStarted.map { Duration.between(testSuiteStartedTime, it) }

    override fun endDelay(): Result<Duration> = lastTestEnded.map { Duration.between(it, testSuiteEndedTime) }

    override fun medianQueueTime(): Result<Duration> =
        countMetric("Cannot calculate median queue time") {
            Duration.ofMillis(
                queueTimes
                    .map { it.toMillis() }
                    .median()
                    .toLong()
            )
        }

    override fun medianInstallationTime(): Result<Duration> =
        countMetric("Cannot calculate median installation time") {
            Duration.ofMillis(
                installationTimes
                    .map { it.toMillis() }
                    .median()
                    .toLong()
            )
        }

    override fun suiteTime(): Result<Duration> =
        firstTestStarted.combine(lastTestEnded) { started, ended -> Duration.between(started, ended) }

    override fun totalTime(): Duration = Duration.between(testSuiteStartedTime, testSuiteEndedTime)

    override fun medianDeviceUtilization(): Result<Percent> =
        countMetric("Cannot calculate median device utilization") {
            deviceWorkerEvents.values.filterIsInstance<DeviceWorkerEvent.Finished>()
                .map { it.utilizationPercent.toInt() }
                .median()
                .toInt()
                .fromZeroToHundredPercent()
        }

    private fun <T> countMetric(errorMsg: String, counter: () -> T): Result<T> =
        Result.tryCatch(counter)
            .rescue { error ->
                Result.Failure(RuntimeException(errorMsg, error))
            }

    private inline fun <T> T?.toResult(lazyMessage: () -> String): Result<T> = when (this) {
        null -> Result.Failure(IllegalStateException(lazyMessage()))
        else -> Result.Success(this)
    }
}
