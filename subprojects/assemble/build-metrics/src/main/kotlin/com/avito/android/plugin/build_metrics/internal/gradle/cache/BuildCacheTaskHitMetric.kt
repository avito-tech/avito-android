package com.avito.android.plugin.build_metrics.internal.gradle.cache

import com.avito.android.graphite.GraphiteMetric
import com.avito.android.plugin.build_metrics.internal.core.BuildMetric
import com.avito.graphite.series.SeriesName

internal class BuildCacheTaskHitMetric(
    private val type: BuildCacheMetricType.TaskType,
    private val hitsCount: Long,
) : BuildMetric.Graphite() {

    private val base: SeriesName = SeriesName.create("gradle.cache.remote.task.type.hit", multipart = true)

    override fun asGraphite(): GraphiteMetric {
        return GraphiteMetric(base.addTag("task_type", type.name), hitsCount.toString())
    }
}
