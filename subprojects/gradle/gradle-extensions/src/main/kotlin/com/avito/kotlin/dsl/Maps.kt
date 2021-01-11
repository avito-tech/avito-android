package com.avito.kotlin.dsl

@Suppress("UNCHECKED_CAST")
fun filterNotBlankValues(map: Map<String, Any?>) =
    map.filterValues { value: Any? -> value?.toString().isNullOrBlank().not() } as Map<String, String>
