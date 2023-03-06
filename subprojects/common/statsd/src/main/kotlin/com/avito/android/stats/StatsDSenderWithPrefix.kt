package com.avito.android.stats

import com.avito.graphite.series.SeriesName

internal class StatsDSenderWithPrefix(
    private val delegate: StatsDSender,
    private val prefix: SeriesName,
) : StatsDSender {

    override fun send(metric: StatsMetric) {
        delegate.send(
            metric.copy(metric.name.prefix(prefix), metric.value)
        )
    }
}
