package com.avito.android.plugin.build_metrics.internal.core

import com.avito.android.graphite.GraphiteMetric
import com.avito.android.stats.StatsMetric

internal sealed class BuildMetric {

    abstract class Statsd : BuildMetric() {
        abstract fun asStatsd(): StatsMetric
    }

    abstract class Graphite : BuildMetric() {
        abstract fun asGraphite(): GraphiteMetric
    }
}
