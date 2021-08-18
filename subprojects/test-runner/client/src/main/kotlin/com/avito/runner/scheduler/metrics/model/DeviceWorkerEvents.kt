package com.avito.runner.scheduler.metrics.model

import com.avito.math.Percent
import com.avito.math.percentOf
import java.time.Duration

internal sealed class DeviceWorkerEvents {

    abstract val created: Duration
    abstract val testExecutionEvents: MutableMap<TestKey, TestExecutionEvent>

    data class Created(
        override val created: Duration,
        override val testExecutionEvents: MutableMap<TestKey, TestExecutionEvent>,
    ) : DeviceWorkerEvents()

    data class Finished(
        override val created: Duration,
        override val testExecutionEvents: MutableMap<TestKey, TestExecutionEvent>,
        val finished: Duration
    ) : DeviceWorkerEvents() {

        private val totalTime: Duration = finished - created

        private val effectiveWorkTime = testExecutionEvents.values.filterIsInstance<TestExecutionEvent.Finished>()
            .map { it.effectiveWorkTime.toMillis() }
            .sum()

        val utilizationPercent: Percent = effectiveWorkTime.percentOf(totalTime.toMillis())
    }

    companion object
}

internal fun DeviceWorkerEvents.finish(finished: Duration): DeviceWorkerEvents = when (this) {
    is DeviceWorkerEvents.Created -> DeviceWorkerEvents.Finished(this.created, this.testExecutionEvents, finished)
    is DeviceWorkerEvents.Finished -> error("$this is in its finished state already")
}
