package com.avito.android.module_graph.models

import kotlinx.serialization.Serializable

@Serializable
public data class ModuleGraphInfo(
    val dependencies: List<ModuleGraphEdge>,
    val sizes: Map<String, Int>,
    val modulesToDemoApps: Map<String, List<String>>,
)

@Serializable
public data class ModuleGraphEdge(
    val from: String,
    val to: String,
    val type: String,
)
