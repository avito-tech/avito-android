package com.avito.android.stats

public sealed class StatsMetric {
    public abstract val name: SeriesName
    public abstract val value: Any
    public abstract val type: String

    public companion object {
        public fun time(name: SeriesName, ms: Long): TimeMetric = TimeMetric(name, ms)
        public fun count(name: SeriesName, value: Long = 1): CountMetric = CountMetric(name, value)
        public fun gaugeLong(name: SeriesName, value: Long): GaugeLongMetric = GaugeLongMetric(name, value)
        public fun gaugeDouble(name: SeriesName, value: Double): GaugeDoubleMetric = GaugeDoubleMetric(name, value)
    }
}

public data class TimeMetric(
    override val name: SeriesName,
    // todo use java.time.api
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
