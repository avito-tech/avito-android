package com.avito.android.plugin.build_metrics.internal.gradle.tasks.critical

import com.avito.android.graphite.GraphiteMetric
import com.avito.android.plugin.build_metrics.internal.core.BuildMetric
import com.avito.graphite.series.SeriesName
import java.time.Duration

internal class BuildCriticalPathTaskCumulativeMetric(
    private val taskType: String,
    private val tasksDuration: Duration,
) : BuildMetric.Graphite() {

    private val base: SeriesName = SeriesName.create("gradle.critical.task.type", multipart = true)

    override fun asGraphite(): GraphiteMetric {
        return GraphiteMetric(
            base.addTag("task_type", taskType),
            tasksDuration.toMillis().toString()
        )
    }
}
