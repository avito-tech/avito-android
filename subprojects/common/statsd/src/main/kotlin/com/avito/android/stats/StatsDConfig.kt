package com.avito.android.stats

import java.io.Serializable

data class StatsDConfig(
    val isEnabled: Boolean,
    val host: String,
    val fallbackHost: String,
    val port: Int,
    val namespace: String
) : Serializable
