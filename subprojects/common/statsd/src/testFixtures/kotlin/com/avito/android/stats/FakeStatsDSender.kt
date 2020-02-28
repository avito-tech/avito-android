package com.avito.android.stats

import com.avito.android.stats.StatsDSender

class FakeStatsdSender: StatsDSender {

    val paths = mutableListOf<String>()
    val metrics = mutableListOf<StatsMetric>()

    override fun send(prefix: String, metric: StatsMetric) {
        paths.add(prefix)
        metrics.add(metric)
    }
}
