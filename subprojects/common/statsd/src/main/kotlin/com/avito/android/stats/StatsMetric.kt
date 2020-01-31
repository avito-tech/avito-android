package com.avito.android.stats

sealed class StatsMetric {
    abstract val path: String
    abstract val value: Any
    abstract val type: String
}

class TimeMetric(
    override val path: String,
    timeInMs: Long
) : StatsMetric() {
    override val value: Long = timeInMs
    override val type = "time"
}

class CountMetric(
    override val path: String,
    delta: Long = 1
) : StatsMetric() {
    override val value: Long = delta
    override val type = "count"
}

class GaugeMetric(
    override val path: String,
    value: Number
) : StatsMetric() {
    override val value: Long = value.toLong()
    override val type = "gauge"
}
