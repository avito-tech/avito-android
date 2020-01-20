package com.avito.android.lint.util

// TODO: memoize
internal fun typeHierarchy(clazz: Class<*>): Collection<String> {
    return try {
        typeHierarchyRecursive(clazz) - clazz.name
    } catch (t: Throwable) {
        println("Unable to load super type or interfaces: $t")
        emptyList()
    }
}

internal fun typeHierarchyRecursive(clazz: Class<*>): Collection<String> {
    if (clazz.name.startsWith("java.")) {
        return emptyList()
    }
    val fromSuper = if (clazz.superclass != null) typeHierarchyRecursive(clazz.superclass) else emptyList()
    val fromInterface = clazz.interfaces.flatMap {
        typeHierarchyRecursive(it)
    }
        .toList()
    return fromSuper + fromInterface + clazz.name
}
