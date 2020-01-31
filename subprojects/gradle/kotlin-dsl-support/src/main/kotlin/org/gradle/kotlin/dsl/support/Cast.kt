package org.gradle.kotlin.dsl.support

@Suppress("unchecked_cast", "nothing_to_inline")
internal
inline fun <T> uncheckedCast(obj: Any?): T =
    obj as T
