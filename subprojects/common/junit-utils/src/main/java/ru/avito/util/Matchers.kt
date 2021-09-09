package ru.avito.util

import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher

// unspecified return type is required to allow compiler to infer type in caller code

@Suppress("HasPlatformType")
public inline fun <reified T : Any> instanceOf(): Matcher<Any> = CoreMatchers.instanceOf(T::class.java)

@Suppress("NOTHING_TO_INLINE")
public inline infix fun <T1 : Any, T2 : Any> T1.classEquals(other: T2): Boolean = this::class == other::class
