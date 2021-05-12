package com.avito.graph

data class SimpleOperation(
    override val id: String,
    override val duration: Double = 1.0,
    override val predecessors: Set<String> = emptySet()
) : Operation {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Operation) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
