package com.avito.android.stats

sealed class StatsMetric {
    abstract val name: SeriesName
    abstract val value: Any
    abstract val type: String
}

data class TimeMetric(
    override val name: SeriesName,
    val timeInMs: Long
) : StatsMetric() {
    override val value: Long = timeInMs
    override val type = "time"
}

data class CountMetric(
    override val name: SeriesName,
    val delta: Long = 1
) : StatsMetric() {
    override val value: Long = delta
    override val type = "count"
}

data class GaugeMetric(
    override val name: SeriesName,
    val gauge: Number
) : StatsMetric() {
    override val value: Long = gauge.toLong()
    override val type = "gauge"
}
