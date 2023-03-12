package com.avito.android.plugin.build_metrics.internal.gradle.tasks.critical

import com.avito.android.graphite.GraphiteMetric
import com.avito.android.plugin.build_metrics.internal.core.BuildMetric
import com.avito.android.plugin.build_metrics.internal.toTagValue
import com.avito.graphite.series.SeriesName
import org.gradle.util.Path
import java.time.Duration

internal class BuildCriticalPathModuleTaskMetric(
    private val moduleName: Path,
    private val taskType: String,
    private val taskDuration: Duration,
) : BuildMetric.Graphite() {

    private val base: SeriesName = SeriesName.create("gradle.critical.module.task.type", multipart = true)

    override fun asGraphite(): GraphiteMetric {
        val series = base
            .addTag("module_name", moduleName.toTagValue())
            .addTag("task_type", taskType)
        return GraphiteMetric(series, taskDuration.toMillis().toString())
    }
}
