package com.avito.graph

public interface Operation {
    public val id: String
    public val duration: Double

    /**
     * ids of predecessor operations
     */
    public val predecessors: Set<String>
}
