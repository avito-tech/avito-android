package com.avito.android.graphite

/**
 * Incubating
 */
class GraphiteMetric(
    val path: String,
    val value: String,
    /**
     * The number of seconds since unix epoch time.
     * Carbon will use the time of arrival if the timestamp is set to -1.
     */
    val timestamp: Long = -1
)
