package com.avito.android.plugin.build_metrics.internal.gradle.total_build

import com.avito.android.graphite.GraphiteMetric
import com.avito.android.plugin.build_metrics.internal.core.BuildMetric
import com.avito.graphite.series.SeriesName

internal class BuildTotalMetric(
    private val valueMs: Long,
) : BuildMetric.Graphite() {

    private val base: SeriesName = SeriesName.create("gradle.build", multipart = true)

    override fun asGraphite(): GraphiteMetric {
        return GraphiteMetric(base, valueMs.toString())
    }
}
