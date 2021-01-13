package com.avito.android.stats

import java.io.Serializable

sealed class StatsDConfig : Serializable {

    object Disabled : StatsDConfig()

    data class Enabled(
        val host: String,
        val fallbackHost: String,
        val port: Int,
        val namespace: String
    ) : StatsDConfig()
}
