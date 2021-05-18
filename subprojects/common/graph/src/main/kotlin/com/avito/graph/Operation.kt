package com.avito.graph

interface Operation {
    val id: String
    val duration: Double

    /**
     * ids of predecessor operations
     */
    val predecessors: Set<String>
}
