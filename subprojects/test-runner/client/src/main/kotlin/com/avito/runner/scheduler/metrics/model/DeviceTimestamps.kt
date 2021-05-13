package com.avito.runner.scheduler.metrics.model

import com.avito.math.percentOf

internal sealed class DeviceTimestamps(
    open val created: Long,
    open val testTimestamps: MutableMap<TestKey, TestTimestamps>,
) {

    data class Started(
        override val created: Long,
        override val testTimestamps: MutableMap<TestKey, TestTimestamps>,
    ) : DeviceTimestamps(created, testTimestamps)

    data class Finished(
        override val created: Long,
        override val testTimestamps: MutableMap<TestKey, TestTimestamps>,
        val finished: Long
    ) : DeviceTimestamps(created, testTimestamps) {

        private val totalTime = finished - created

        private val effectiveWorkTime = testTimestamps.values.filterIsInstance<TestTimestamps.Finished>()
            .map { it.effectiveWorkTime }
            .sum()

        val utilizationPercent: Int = effectiveWorkTime.percentOf(totalTime).toInt()
    }

    companion object
}

internal fun DeviceTimestamps.finish(finished: Long): DeviceTimestamps = when (this) {
    is DeviceTimestamps.Started -> DeviceTimestamps.Finished(this.created, this.testTimestamps, finished)
    is DeviceTimestamps.Finished -> error("$this is in its finished state already")
}
