package com.avito.android.plugin.build_metrics.internal.runtime.jvm

import com.avito.android.graphite.GraphiteMetric
import com.avito.android.plugin.build_metrics.internal.core.BuildMetric
import com.avito.graphite.series.SeriesName

internal class BuildJvmMetaspaceUsedMetric(
    private val processName: String,
    private val valueKb: Long,
) : BuildMetric.Graphite() {

    private val base: SeriesName = SeriesName.create("jvm.memory.metaspace.used", multipart = true)

    override fun asGraphite(): GraphiteMetric {
        return GraphiteMetric(base.addTag("process_name", processName), valueKb.toString())
    }
}
