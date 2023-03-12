package com.avito.android.plugin.build_metrics.internal.gradle.tasks.slow

import com.avito.android.graphite.GraphiteMetric
import com.avito.android.plugin.build_metrics.internal.core.BuildMetric
import com.avito.graphite.series.SeriesName

internal class BuildTasksCumulativeMetric(
    private val timeMs: Long,
) : BuildMetric.Graphite() {

    private val base: SeriesName = SeriesName.create("gradle.tasks", multipart = true)

    override fun asGraphite(): GraphiteMetric {
        return GraphiteMetric(base, timeMs.toString())
    }
}
