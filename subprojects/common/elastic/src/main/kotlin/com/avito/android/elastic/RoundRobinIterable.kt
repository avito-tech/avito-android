package com.avito.android.elastic

internal class RoundRobinIterable<out T>(private val collection: Iterable<T>) {

    private var iterator: Iterator<T> = collection.iterator()

    operator fun next(): T {
        if (!iterator.hasNext()) {
            iterator = collection.iterator()
        }
        return iterator.next()
    }
}
