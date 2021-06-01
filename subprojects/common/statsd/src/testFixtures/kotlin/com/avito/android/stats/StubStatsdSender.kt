package com.avito.android.stats

class StubStatsdSender : StatsDSender {

    private val sentMetrics = mutableListOf<StatsMetric>()

    override fun send(metric: StatsMetric) {
        sentMetrics += metric
    }

    override fun send(prefix: SeriesName, metric: StatsMetric) {
        TODO("Will be deleted")
    }

    fun getSentMetrics(): List<StatsMetric> = sentMetrics
}
