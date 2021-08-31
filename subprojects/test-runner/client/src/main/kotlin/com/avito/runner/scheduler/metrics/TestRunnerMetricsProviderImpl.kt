package com.avito.runner.scheduler.metrics

import com.avito.android.Result
import com.avito.math.median
import com.avito.runner.scheduler.metrics.model.DeviceWorkerState
import com.avito.runner.scheduler.metrics.model.TestExecutionState
import java.time.Duration
import java.time.Instant

internal data class TestRunnerMetricsProviderImpl(
    private val testSuiteStartedTime: Instant,
    private val testSuiteEndedTime: Instant,
    private val deviceWorkerStates: Set<DeviceWorkerState>
) : TestRunnerMetricsProvider {

    private val testTimestamps = deviceWorkerStates.flatMap { it.testExecutionStates() }

    // TODO lost failed, died devices
    private val finishedDevices = deviceWorkerStates.filterIsInstance<DeviceWorkerState.Finished>()

    private val firstTestStarted: Result<Instant> = testTimestamps.filterIsInstance<TestExecutionState.Completed>()
        .map { it.testStarted }
        .minOrNull()
        .toResult { "Cannot calculate first started test time" }

    private val lastTestEnded: Result<Instant> = testTimestamps.filterIsInstance<TestExecutionState.Completed>()
        .map { it.completed }
        .maxOrNull()
        .toResult { "Cannot calculate last ended test time" }

    private val queueTimes: List<Duration> =
        testTimestamps
            .filterIsInstance<TestExecutionState.Completed>()
            .map { Duration.between(testSuiteStartedTime, it.intentionReceived) }

    private val installationTimes: List<Duration> =
        testTimestamps
            .filterIsInstance<TestExecutionState.Completed>()
            .map { it.installationTime }

    private val devicesLivingTime: Duration =
        Duration.ofMillis(
            finishedDevices.sumOf { it.livingTime.toMillis() }
        )

    private val devicesWorkingTime: Duration =
        Duration.ofMillis(
            finishedDevices.sumOf { it.workingTime.toMillis() }
        )

    private val devicesIdleTime: Duration =
        Duration.ofMillis(
            finishedDevices.sumOf { it.idleTime.toMillis() }
        )

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

    override fun devicesLiving(): Duration = devicesLivingTime

    override fun devicesWorking(): Duration = devicesWorkingTime

    override fun devicesIdle(): Duration = devicesIdleTime

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
