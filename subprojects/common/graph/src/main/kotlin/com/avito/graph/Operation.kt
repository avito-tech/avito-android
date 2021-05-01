package com.avito.graph

interface Operation {
    val id: String
    val duration: Double
    val predecessors: Set<String>
}

fun Operation.isLeaf() =
    predecessors.isEmpty()
