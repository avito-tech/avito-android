package com.avito.test

internal fun <T : Any> listWithDefault(size: Int, value: T): List<T> {
    return (0 until size).map { value }
}
