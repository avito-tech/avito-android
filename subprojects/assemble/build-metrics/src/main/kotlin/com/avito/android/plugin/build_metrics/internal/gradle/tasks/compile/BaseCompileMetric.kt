package com.avito.android.plugin.build_metrics.internal.gradle.tasks.compile

import com.avito.android.graphite.GraphiteMetric
import com.avito.android.plugin.build_metrics.internal.core.BuildMetric
import com.avito.graphite.series.SeriesName

internal sealed class BaseCompileMetric : BuildMetric.Graphite() {

    protected abstract val time: Long
    protected abstract val moduleName: String
    protected abstract val taskName: String
    protected abstract val taskType: String

    override fun asGraphite(): GraphiteMetric {
        return GraphiteMetric(
            path = SeriesName
                .create("gradle.task.type.$taskType", multipart = true)
                .addTag("module_name", moduleName)
                .addTag("task_name", taskName),
            value = time.toString()
        )
    }
}
