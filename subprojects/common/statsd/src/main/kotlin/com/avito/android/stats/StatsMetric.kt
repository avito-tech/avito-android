package com.avito.android.stats

/**
 * [Statsd metrics](https://github.com/statsd/statsd/blob/master/docs/metric_types.md#statsd-metric-types)
 */
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

/**
 * [Timing](https://github.com/statsd/statsd/blob/master/docs/metric_types.md#timing)
 */
public data class TimeMetric(
    override val name: SeriesName,
    // todo use java.time.api
    public val timeInMs: Long
) : StatsMetric() {
    override val value: Long = timeInMs
    override val type: String = "time"
}

/**
 * [Counting](https://github.com/statsd/statsd/blob/master/docs/metric_types.md#counting)
 */
public data class CountMetric(
    override val name: SeriesName,
    public val delta: Long = 1
) : StatsMetric() {
    override val value: Long = delta
    override val type: String = "count"
}

/**
 * [Gauges](https://github.com/statsd/statsd/blob/master/docs/metric_types.md#gauges)
 */
public data class GaugeLongMetric(
    override val name: SeriesName,
    public val gauge: Long
) : StatsMetric() {
    override val value: Long = gauge
    override val type: String = "gauge"
}

/**
 * [Gauges](https://github.com/statsd/statsd/blob/master/docs/metric_types.md#gauges)
 */
public data class GaugeDoubleMetric(
    override val name: SeriesName,
    public val gauge: Double
) : StatsMetric() {
    override val value: Double = gauge
    override val type: String = "gauge"
}
