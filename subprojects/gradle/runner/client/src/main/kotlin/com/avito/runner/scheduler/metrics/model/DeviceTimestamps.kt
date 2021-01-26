package com.avito.runner.scheduler.metrics.model

import com.avito.math.percentOf

internal data class DeviceTimestamps(
    val created: Long,
    val testTimestamps: MutableMap<TestKey, TestTimestamps>,
    val finished: Long
) {

    private val totalTime
        get() = finished - created

    private val effectiveWorkTime
        get() = testTimestamps.values.mapNotNull { it.effectiveWorkTime }.sum()

    val utilizationPercent: Int?
        get() = if (totalTime > effectiveWorkTime) {
            effectiveWorkTime.percentOf(totalTime).toInt()
        } else {
            null // in case if device died and finish time not logged
        }

    companion object
}
