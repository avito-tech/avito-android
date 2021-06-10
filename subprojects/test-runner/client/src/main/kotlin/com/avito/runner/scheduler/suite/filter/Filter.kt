package com.avito.runner.scheduler.suite.filter

import java.io.Serializable

public class Filter<T : Serializable> {

    public data class Value<T : Serializable>(
        val included: Set<T>,
        val excluded: Set<T>
    ) : Serializable

    private var included = emptySet<T>()

    private var excluded = emptySet<T>()

    public val value: Value<T>
        get() {
            return Value(
                included = included.toSet(),
                excluded = excluded.toSet()
            )
        }

    public fun include(values: Set<T>) {
        included = values
    }

    public fun exclude(values: Set<T>) {
        excluded = values
    }
}
