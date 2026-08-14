package com.avito.android.module_graph.models

import kotlinx.serialization.Serializable

@Serializable
public data class ModuleGraphInfo(
    val dependencies: List<ModuleGraphEdge>,
    val applications: List<String>,
    val linesOfCode: Map<String, ModuleLinesOfCode>,
    val transitiveLinesOfCode: Map<String, Int>,
    val impactedApplications: Map<String, List<String>>,
)

@Serializable
public data class ModuleLinesOfCode(
    val main: Int,
    val test: Int? = null,
    val androidTest: Int? = null,
)

@Serializable
public data class ModuleGraphEdge(
    val from: String,
    val to: String,
    val type: String,
)
