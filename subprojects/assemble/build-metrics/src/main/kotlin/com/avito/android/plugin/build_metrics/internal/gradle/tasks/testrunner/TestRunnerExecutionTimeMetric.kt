package com.avito.android.plugin.build_metrics.internal.gradle.tasks.testrunner

import com.avito.android.graphite.GraphiteMetric
import com.avito.android.plugin.build_metrics.internal.core.BuildMetric
import com.avito.graphite.series.SeriesName

internal class TestRunnerExecutionTimeMetric(
    private val moduleName: String,
    private val tags: Map<String, String>,
    private val timeMs: Long,
) : BuildMetric.Graphite() {

    private val base: SeriesName = SeriesName.create("gradle.tests.execution_time.module", multipart = true)

    override fun asGraphite(): GraphiteMetric {
        return GraphiteMetric(base.addTag("module_name", moduleName).addTags(tags), timeMs.toString())
    }
}
