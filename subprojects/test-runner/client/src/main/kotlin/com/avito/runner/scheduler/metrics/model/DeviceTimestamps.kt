package com.avito.runner.scheduler.metrics.model

import com.avito.math.percentOf

internal data class DeviceTimestamps(
    val created: Long? = null,
    val testTimestamps: MutableMap<TestKey, TestTimestamps>,
    val finished: Long? = null
) {

    private val totalTime: Long?
        get() = if (finished != null && created != null) {
            finished - created
        } else {
            null
        }

    private val effectiveWorkTime
        get() = testTimestamps.values.mapNotNull { it.effectiveWorkTime }.sum()

    val utilizationPercent: Int?
        get() {
            val localTotalTime = totalTime
            return if (localTotalTime != null) {
                effectiveWorkTime.percentOf(localTotalTime).toInt()
            } else {
                null // in case if device died and finish time not logged
            }
        }

    companion object
}
