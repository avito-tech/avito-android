package com.avito.android.graphite

import java.io.Serializable

data class GraphiteConfig(
    /**
     * Set false if you want to disable transport and use dummy implementation.
     */
    val isEnabled: Boolean,
    /**
     * Debug mode with detailed logs
     */
    val debug: Boolean,
    val host: String,
    val port: Int,
    /**
     * It is a common prefix to all metrics.
     *
     * For example: namespace "apps.android" with a metric "build_time"
     * will result in "app.android.build_time" in graphite.
     */
    val namespace: String
) : Serializable
