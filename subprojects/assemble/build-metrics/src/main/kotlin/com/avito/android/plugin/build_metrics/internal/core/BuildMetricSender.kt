package com.avito.android.plugin.build_metrics.internal.core

import com.avito.android.graphite.GraphiteSender
import com.avito.android.stats.StatsDSender

internal interface BuildMetricSender {

    fun send(metric: BuildMetric)

    private class Impl(
        private val statsDSender: StatsDSender,
        private val graphiteSender: GraphiteSender,
    ) : BuildMetricSender {

        override fun send(metric: BuildMetric) {
            when (metric) {
                is BuildMetric.Statsd -> statsDSender.send(metric.asStatsd())
                is BuildMetric.Graphite -> graphiteSender.send(metric.asGraphite())
            }
        }
    }

    companion object {
        fun create(
            statsdSender: StatsDSender,
            graphiteSender: GraphiteSender
        ): BuildMetricSender {
            return Impl(statsdSender, graphiteSender)
        }
    }
}
