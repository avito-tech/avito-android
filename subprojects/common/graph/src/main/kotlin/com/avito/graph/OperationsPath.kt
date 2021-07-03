package com.avito.graph

public class OperationsPath<T : Operation>(
    public val operations: List<T>
) {

    public val start: T
        get() = operations.first()

    public val end: T
        get() = operations.last()

    public val duration: Double =
        operations.sumByDouble { it.duration }
}
