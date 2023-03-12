package com.avito.android.plugin.build_metrics.internal.runtime.os

import com.avito.android.graphite.GraphiteMetric
import com.avito.android.plugin.build_metrics.internal.core.BuildMetric
import com.avito.graphite.series.SeriesName

internal class BuildOsMemoryUsedMetric(
    private val valueKb: Long,
) : BuildMetric.Graphite() {

    private val base: SeriesName = SeriesName.create("os.memory.used", multipart = true)

    override fun asGraphite(): GraphiteMetric {
        return GraphiteMetric(base, valueKb.toString())
    }
}
