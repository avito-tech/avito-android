package com.avito.android.plugin.build_metrics.internal.core

import com.avito.android.graphite.GraphiteMetric
import com.avito.android.stats.StatsMetric

internal class StubBuildMetricsSender : BuildMetricSender {
    private val sentStatsdMetrics = mutableListOf<StatsMetric>()
    private val sentGraphiteMetrics = mutableListOf<GraphiteMetric>()

    override fun send(metric: BuildMetric) {
        when (metric) {
            is BuildMetric.Graphite -> sentGraphiteMetrics += metric.asGraphite()
            is BuildMetric.Statsd -> sentStatsdMetrics += metric.asStatsd()
        }
    }

    fun getSentStatsdMetrics(): List<StatsMetric> = sentStatsdMetrics
    fun getSentGraphiteMetrics(): List<GraphiteMetric> = sentGraphiteMetrics
}
