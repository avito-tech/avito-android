package com.avito.kotlin.dsl

@Suppress("UNCHECKED_CAST")
public fun filterNotBlankValues(map: Map<String, Any?>): Map<String, String> =
    map.filterValues { value: Any? -> value?.toString().isNullOrBlank().not() } as Map<String, String>
