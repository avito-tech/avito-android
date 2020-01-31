package com.avito.impact.util

/**
 *  Synthetic interface.
 *  It's needed to allow delegated implementation of equals & hashCode
 */
internal interface Equality {
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}
