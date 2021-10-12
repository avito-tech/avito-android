package com.avito.runner.scheduler.metrics.model

import com.avito.runner.service.worker.device.DeviceCoordinate
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

internal sealed class DeviceWorkerState {

    abstract val created: Instant
    abstract val key: DeviceCoordinate
    protected abstract val testExecutionStates: ConcurrentHashMap.KeySetView<TestExecutionState, Boolean>

    fun testExecutionStates(): Set<TestExecutionState> {
        return testExecutionStates.toSet()
    }

    abstract fun finish(finished: Instant): Finished

    abstract fun testIntentionReceived(testKey: TestKey, time: Instant)

    abstract fun testStarted(testKey: TestKey, time: Instant)

    abstract fun testCompleted(testKey: TestKey, time: Instant)

    override fun equals(other: Any?): Boolean {
        return key == (other as? DeviceWorkerState)?.key
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }

    abstract fun testIntentionFailed(testKey: TestKey)

    class Created(
        override val created: Instant,
        override val testExecutionStates: ConcurrentHashMap.KeySetView<TestExecutionState, Boolean>,
        override val key: DeviceCoordinate,
    ) : DeviceWorkerState() {

        override fun testIntentionReceived(testKey: TestKey, time: Instant) {
            check(testExecutionStates.singleOrNull { it.test == testKey } == null) {
                "Intention $testKey already has been received"
            }
            testExecutionStates.add(TestExecutionState.IntentionReceived(time, testKey))
        }

        override fun testIntentionFailed(testKey: TestKey) {
            val state = getTestState<TestExecutionState.IntentionReceived>(testKey)
            testExecutionStates.remove(state)
        }

        override fun testStarted(testKey: TestKey, time: Instant) {
            val state = getTestState<TestExecutionState.IntentionReceived>(testKey)
            val newState = state.start(time)
            replace(state, newState)
        }

        override fun testCompleted(testKey: TestKey, time: Instant) {
            val state = getTestState<TestExecutionState.Started>(testKey)
            val newState = state.complete(time)
            replace(state, newState)
        }

        private inline fun <reified T : TestExecutionState> getTestState(testKey: TestKey): T {
            val value = testExecutionStates.singleOrNull { it.test == testKey }
            return checkNotNull(value as? T) {
                "Can't find ${T::class.java.simpleName} for $testKey"
            }
        }

        private fun replace(old: TestExecutionState, new: TestExecutionState) {
            testExecutionStates.remove(old)
            testExecutionStates.add(new)
        }

        override fun finish(finished: Instant) = Finished(created, testExecutionStates, finished, key)
    }

    class Finished(
        override val created: Instant,
        override val testExecutionStates: ConcurrentHashMap.KeySetView<TestExecutionState, Boolean>,
        val finished: Instant,
        override val key: DeviceCoordinate,
    ) : DeviceWorkerState() {

        val livingTime: Duration = Duration.between(created, finished)

        val workingTime: Duration = Duration.ofMillis(
            testExecutionStates.filterIsInstance<TestExecutionState.Completed>()
                .map { it.processingTime }
                .sumOf { it.toMillis() }
        )

        val idleTime: Duration = livingTime - workingTime

        override fun testIntentionReceived(testKey: TestKey, time: Instant) {
            throw UnsupportedOperationException("Can't modify DeviceWorkerState.Finished")
        }

        override fun testIntentionFailed(testKey: TestKey) {
            throw UnsupportedOperationException("Can't modify DeviceWorkerState.Finished")
        }

        override fun testStarted(testKey: TestKey, time: Instant) {
            throw UnsupportedOperationException("Can't modify DeviceWorkerState.Finished")
        }

        override fun testCompleted(testKey: TestKey, time: Instant) {
            throw UnsupportedOperationException("Can't modify DeviceWorkerState.Finished")
        }

        override fun finish(finished: Instant): Finished {
            throw UnsupportedOperationException("Can't finish DeviceWorkerState.Finished")
        }
    }

    companion object
}
