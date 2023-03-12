package com.avito.android.plugin.build_metrics.internal.gradle.cache

import com.avito.android.graphite.GraphiteMetric
import com.avito.android.plugin.build_metrics.internal.core.BuildMetric
import com.avito.graphite.series.SeriesName

internal class BuildCacheHitMetric(
    private val hitsCount: Long,
) : BuildMetric.Graphite() {

    private val base: SeriesName = SeriesName.create("gradle.cache.remote.hit", multipart = true)

    override fun asGraphite(): GraphiteMetric {
        return GraphiteMetric(base, hitsCount.toString())
    }
}
