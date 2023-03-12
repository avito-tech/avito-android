package com.avito.android.graphite

import com.avito.graphite.series.SeriesName
import java.time.Instant

/**
 * Incubating
 */
public class GraphiteMetric private constructor(
    public val path: SeriesName,
    public val value: String,
    public val timeInSec: Long,
) {
    public constructor(
        path: SeriesName,
        value: String,
        /**
         * The number of seconds since unix epoch time.
         * Carbon will use the time of arrival if the timestamp is set to -1.
         */
        time: Instant? = null,
    ) : this(path, value, time?.epochSecond ?: -1)

    internal fun withPrefix(seriesName: SeriesName): GraphiteMetric {
        return GraphiteMetric(
            seriesName.append(path),
            value,
            timeInSec
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GraphiteMetric

        if (path != other.path) return false
        if (value != other.value) return false
        if (timeInSec != other.timeInSec) return false

        return true
    }

    override fun hashCode(): Int {
        var result = path.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + timeInSec.hashCode()
        return result
    }

    override fun toString(): String {
        return "GraphiteMetric(path=$path, value='$value', timeInSec=$timeInSec)"
    }
}
