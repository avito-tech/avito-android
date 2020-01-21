/*
 * Copyright 2015-2019 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Modifications copyright (C) 2020 Avito
 */

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
