@file:Suppress("NOTHING_TO_INLINE")

package ru.avito.util

import org.hamcrest.CoreMatchers

@Suppress("HasPlatformType") // unspecified return type is required to allow compiler to infer type in caller code
inline fun <reified T : Any> instanceOf() = CoreMatchers.instanceOf<Any>(T::class.java)

inline infix fun <T1 : Any, T2 : Any> T1.classEquals(other: T2): Boolean = this::class == other::class
