package com.avito.android.plugin.build_metrics.internal.runtime.os

import com.avito.android.plugin.build_metrics.internal.core.BuildMetricSender

internal interface OsMetricsSender {
    fun send(memoryInfo: MemoryInfo)
}

internal class OsMetricsSenderImpl(
    private val tracker: BuildMetricSender
) : OsMetricsSender {

    override fun send(memoryInfo: MemoryInfo) {
        tracker.send(
            BuildOsMemoryUsedMetric(
                memoryInfo.usedKb.toLong()
            )
        )
        tracker.send(
            BuildOsMemoryTotalMetric(
                memoryInfo.totalKb.toLong()
            )
        )
    }
}
