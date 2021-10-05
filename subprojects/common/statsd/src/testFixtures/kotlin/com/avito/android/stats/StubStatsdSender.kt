package com.avito.android.stats

public class StubStatsdSender : StatsDSender {

    private val sentMetrics = mutableListOf<StatsMetric>()

    override fun send(metric: StatsMetric) {
        sentMetrics += metric
    }

    public fun getSentMetrics(): List<StatsMetric> = sentMetrics
}
