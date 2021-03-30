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

// TODO: delete after 2021.12
@Deprecated(
    "Use typed alternative: [GaugeLongMetric] or [GaugeDoubleMetric]. Statsd can't consume any Number.",
)
data class GaugeMetric(
    override val name: SeriesName,
    val gauge: Number
) : StatsMetric() {
    override val value: Number = gauge
    override val type = "gauge"
}

data class GaugeLongMetric(
    override val name: SeriesName,
    val gauge: Long
) : StatsMetric() {
    override val value: Long = gauge
    override val type = "gauge"
}

data class GaugeDoubleMetric(
    override val name: SeriesName,
    val gauge: Double
) : StatsMetric() {
    override val value: Double = gauge
    override val type = "gauge"
}
