package com.avito.android.graphite

/**
 * Incubating
 */
public class GraphiteMetric(
    public val path: String,
    public val value: String,
    /**
     * The number of seconds since unix epoch time.
     * Carbon will use the time of arrival if the timestamp is set to -1.
     */
    public val timestamp: Long = -1
)
