package com.avito.android.graphite

import java.io.Serializable

data class GraphiteConfig(
    val isEnabled: Boolean,
    val host: String,
    val port: Int,
    val namespace: String
) : Serializable
