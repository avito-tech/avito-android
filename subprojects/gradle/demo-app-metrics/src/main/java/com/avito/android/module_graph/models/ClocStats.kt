package com.avito.android.module_graph.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ClocStats(
    val code: Int = 0,
)

@Serializable
public data class ClocOutput(
    @SerialName("Kotlin") val kotlin: ClocStats = ClocStats(),
    @SerialName("XML") val xml: ClocStats = ClocStats(),
)
