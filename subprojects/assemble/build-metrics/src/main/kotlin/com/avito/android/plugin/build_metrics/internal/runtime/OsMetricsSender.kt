package com.avito.android.plugin.build_metrics.internal.runtime

import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDSender
import com.avito.android.stats.TimeMetric

internal interface OsMetricsSender {
    fun send(memoryInfo: MemoryInfo)
}

internal class OsMetricsSenderImpl(
    private val tracker: StatsDSender
) : OsMetricsSender {

    override fun send(memoryInfo: MemoryInfo) {
        tracker.send(
            TimeMetric(
                SeriesName.create("os", "memory", "used"),
                memoryInfo.usedKb.toLong()
            )
        )
        tracker.send(
            TimeMetric(
                SeriesName.create("os", "memory", "total"),
                memoryInfo.totalKb.toLong()
            )
        )
    }
}
