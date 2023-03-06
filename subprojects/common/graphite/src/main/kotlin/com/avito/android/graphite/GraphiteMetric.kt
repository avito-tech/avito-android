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
}
