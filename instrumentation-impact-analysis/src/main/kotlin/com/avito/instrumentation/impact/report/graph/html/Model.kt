package com.avito.instrumentation.impact.report.graph.html

internal data class CytoscapeNode<T>(
    val data: T
)

internal data class CytoscapeNodeData(
    val id: String,
    val group: String,
    val modified: String,
    val primaryModified: String
)

internal data class CytoscapeEdgeData(
    val source: String,
    val target: String,
    val modified: String,
    val primaryModified: String,
    val id: String = "$source$target"
)
