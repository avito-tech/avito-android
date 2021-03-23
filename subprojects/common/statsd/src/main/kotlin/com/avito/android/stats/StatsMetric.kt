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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TimeMetric

        if (name != other.name) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }

    override fun toString(): String {
        return "TimeMetric(name=$name, value=$value)"
    }
}

class CountMetric(
    override val name: SeriesName,
    delta: Long = 1
) : StatsMetric() {
    override val value: Long = delta
    override val type = "count"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CountMetric

        if (name != other.name) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }

    override fun toString(): String {
        return "CountMetric(name=$name, value=$value)"
    }
}

class GaugeMetric(
    override val name: SeriesName,
    value: Number
) : StatsMetric() {
    override val value: Long = value.toLong()
    override val type = "gauge"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GaugeMetric

        if (name != other.name) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }

    override fun toString(): String {
        return "GaugeMetric(name=$name, value=$value)"
    }
}
