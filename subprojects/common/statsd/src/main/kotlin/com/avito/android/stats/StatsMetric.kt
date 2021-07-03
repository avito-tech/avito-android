package com.avito.android.stats

public sealed class StatsMetric {
    public abstract val name: SeriesName
    public abstract val value: Any
    public abstract val type: String
}

public data class TimeMetric(
    override val name: SeriesName,
    public val timeInMs: Long
) : StatsMetric() {
    override val value: Long = timeInMs
    override val type: String = "time"
}

public data class CountMetric(
    override val name: SeriesName,
    public val delta: Long = 1
) : StatsMetric() {
    override val value: Long = delta
    override val type: String = "count"
}

public data class GaugeLongMetric(
    override val name: SeriesName,
    public val gauge: Long
) : StatsMetric() {
    override val value: Long = gauge
    override val type: String = "gauge"
}

public data class GaugeDoubleMetric(
    override val name: SeriesName,
    public val gauge: Double
) : StatsMetric() {
    override val value: Double = gauge
    override val type: String = "gauge"
}
