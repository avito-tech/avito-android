package com.avito.android.elastic

import java.io.Serializable
import java.net.URL

public sealed class ElasticConfig : Serializable {

    public object Disabled : ElasticConfig()

    public data class Enabled(
        val endpoints: List<URL>,
        val indexPattern: String,
        val buildId: String,
        val checkDateFormatter: Boolean
    ) : ElasticConfig()
}
