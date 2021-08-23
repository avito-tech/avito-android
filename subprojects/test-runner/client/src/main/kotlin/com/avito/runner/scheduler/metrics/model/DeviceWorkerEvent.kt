package com.avito.runner.scheduler.metrics.model

import com.avito.math.Percent
import com.avito.math.percentOf
import java.time.Duration
import java.time.Instant

internal sealed class DeviceWorkerEvent {

    abstract val created: Instant
    abstract val testExecutionEvents: MutableMap<TestKey, TestExecutionEvent>

    data class Created(
        override val created: Instant,
        override val testExecutionEvents: MutableMap<TestKey, TestExecutionEvent>,
    ) : DeviceWorkerEvent()

    data class Finished(
        override val created: Instant,
        override val testExecutionEvents: MutableMap<TestKey, TestExecutionEvent>,
        val finished: Instant
    ) : DeviceWorkerEvent() {

        private val totalTime: Duration = Duration.between(created, finished)

        private val effectiveWorkTime = testExecutionEvents.values.filterIsInstance<TestExecutionEvent.Finished>()
            .map { it.effectiveWorkTime.toMillis() }
            .sum()

        val utilizationPercent: Percent = effectiveWorkTime.percentOf(totalTime.toMillis())
    }

    companion object
}

internal fun DeviceWorkerEvent.finish(finished: Instant): DeviceWorkerEvent = when (this) {
    is DeviceWorkerEvent.Created -> DeviceWorkerEvent.Finished(this.created, this.testExecutionEvents, finished)
    is DeviceWorkerEvent.Finished -> error("$this is in its finished state already")
}
