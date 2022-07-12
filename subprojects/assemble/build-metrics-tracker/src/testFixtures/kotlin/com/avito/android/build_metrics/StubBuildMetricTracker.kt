package com.avito.android.build_metrics

import com.avito.android.stats.StatsMetric

public class StubBuildMetricTracker : BuildMetricTracker {

    val trackedMetrics = mutableListOf<StatsMetric>()
    val trackedMetricsWithBuildStatus = mutableListOf<Pair<BuildStatus, StatsMetric>>()

    override fun track(status: BuildStatus, metric: StatsMetric) {
        trackedMetricsWithBuildStatus.add(status to metric)
    }

    override fun track(metric: StatsMetric) {
        trackedMetrics.add(metric)
    }
}
