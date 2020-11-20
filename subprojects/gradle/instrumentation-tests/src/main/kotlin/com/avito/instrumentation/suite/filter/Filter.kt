package com.avito.instrumentation.suite.filter

import java.io.Serializable

class Filter<T : Serializable> {

    data class Value<T : Serializable>(
        val included: Set<T>,
        val excluded: Set<T>
    ) : Serializable

    private var included = emptySet<T>()
    private var excluded = emptySet<T>()

    fun include(values: Set<T>) {
        included = values
    }

    fun exclude(values: Set<T>) {
        excluded = values
    }

    internal val value: Value<T>
        get() {
            return Value(
                included = included.toSet(),
                excluded = excluded.toSet()
            )
        }
}
