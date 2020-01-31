package com.avito.bytecode.graph

class OneDirectedGraph<T> {
    private val data: MutableMap<T, List<T>> = HashMap()

    @Synchronized
    fun putEdge(from: T, to: T) {
        val fromNodeReference = data.getOrEmpty(from)
        data[from] = fromNodeReference.plus(to)

        if (!data.containsKey(to)) {
            data[to] = emptyList()
        }
    }

    @Synchronized
    fun findAllAccessibleNodes(from: T, predicate: (T) -> Boolean): MutableSet<T> {
        val result: MutableSet<T> = mutableSetOf()
        val visited: MutableSet<T> = mutableSetOf()

        findAllAccessibleNodesRecursive(
            from = from,
            predicate = predicate,
            result = result,
            visited = visited
        )

        return result
    }

    private fun findAllAccessibleNodesRecursive(
        from: T,
        predicate: (T) -> Boolean,
        result: MutableSet<T>,
        visited: MutableSet<T>
    ) {
        if (visited.contains(from)) {
            return
        }

        val items = data.getOrEmpty(from)

        if (predicate(from)) {
            result.add(from)
        }

        visited.add(from)

        items.forEach {
            findAllAccessibleNodesRecursive(
                from = it,
                predicate = predicate,
                result = result,
                visited = visited
            )
        }
    }
}

private fun <K, V> Map<K, List<V>>.getOrEmpty(key: K): List<V> = getOrDefault(key, emptyList())
