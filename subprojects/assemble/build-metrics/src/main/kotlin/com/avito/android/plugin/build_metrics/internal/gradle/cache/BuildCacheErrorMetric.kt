package com.avito.android.plugin.build_metrics.internal.gradle.cache

import com.avito.android.graphite.GraphiteMetric
import com.avito.android.plugin.build_metrics.internal.RemoteBuildCacheError
import com.avito.android.plugin.build_metrics.internal.core.BuildMetric
import com.avito.graphite.series.SeriesName

internal class BuildCacheErrorMetric(
    private val error: RemoteBuildCacheError.Selector,
    private val count: Long,
) : BuildMetric.Graphite() {

    private val base: SeriesName = SeriesName.create("gradle.cache.errors", multipart = true)

    override fun asGraphite(): GraphiteMetric {
        val series = base
            .addTag("operation_type", error.code)
            .addTag("error_type", error.errorType)

        return GraphiteMetric(series, count.toString())
    }
}
