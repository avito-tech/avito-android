package com.avito.android.plugin.build_metrics.internal.gradle.cache

import com.avito.android.graphite.GraphiteMetric
import com.avito.android.plugin.build_metrics.internal.core.BuildMetric
import com.avito.graphite.series.SeriesName

internal class BuildCacheModuleTaskHitMetric(
    private val type: BuildCacheMetricType.ModuleTaskType,
    private val hitsCount: Long,
) : BuildMetric.Graphite() {

    private val base: SeriesName = SeriesName.create("gradle.cache.remote.module.task.type.hit", multipart = true)

    override fun asGraphite(): GraphiteMetric {
        return GraphiteMetric(
            base.addTag("module_name", type.moduleName)
                .addTag("task_type", type.taskType),
            hitsCount.toString()
        )
    }
}
