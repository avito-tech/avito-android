package com.avito.android.plugin.build_metrics.internal.gradle.tasks.slow

import com.avito.android.graphite.GraphiteMetric
import com.avito.android.plugin.build_metrics.internal.core.BuildMetric
import com.avito.graphite.series.SeriesName

internal class BuildSlowModuleTaskMetric(
    private val type: SlowMetricType.ModuleTaskType,
    private val timeMs: Long,
) : BuildMetric.Graphite() {

    private val base: SeriesName = SeriesName.create("gradle.slow.module.task.type", multipart = true)

    override fun asGraphite(): GraphiteMetric {
        return GraphiteMetric(
            base
                .addTag("module_name", type.moduleName)
                .addTag("task_type", type.taskType),
            timeMs.toString()
        )
    }
}
