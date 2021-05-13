package com.avito.graph

class OperationsPath<T : Operation>(
    val operations: List<T>
) {

    val start: T
        get() = operations.first()

    val end: T
        get() = operations.last()

    val duration: Double =
        operations.sumByDouble { it.duration }

}
