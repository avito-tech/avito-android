package com.avito.android.stats

import com.avito.graphite.series.SeriesName
import java.io.Serializable

public sealed class StatsDConfig : Serializable {

    public object Disabled : StatsDConfig()

    public data class Enabled(
        val host: String,
        val fallbackHost: String,
        val port: Int,
        val namespace: SeriesName
    ) : StatsDConfig()
}
