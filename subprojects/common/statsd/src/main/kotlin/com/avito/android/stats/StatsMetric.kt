package com.avito.android.stats

sealed class StatsMetric {
    abstract val name: SeriesName
    abstract val value: Any
    abstract val type: String
}

class TimeMetric(
    override val name: SeriesName,
    timeInMs: Long
) : StatsMetric() {
    override val value: Long = timeInMs
    override val type = "time"
}

class CountMetric(
    override val name: SeriesName,
    delta: Long = 1
) : StatsMetric() {
    override val value: Long = delta
    override val type = "count"
}

class GaugeMetric(
    override val name: SeriesName,
    value: Number
) : StatsMetric() {
    override val value: Long = value.toLong()
    override val type = "gauge"
}
