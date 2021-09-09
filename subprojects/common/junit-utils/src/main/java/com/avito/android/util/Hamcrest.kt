package com.avito.android.util

import org.hamcrest.Matcher
import org.hamcrest.Matchers.anyOf
import org.hamcrest.Matchers.not
import org.hamcrest.core.Is

// unspecified return type is required to allow compiler to infer type in caller code

@Suppress(
    "HasPlatformType",
    "FunctionName"
)
public fun <T> Is(matcher: Matcher<T>): Matcher<T> = Is.`is`(matcher)

@Suppress(
    "HasPlatformType",
    "FunctionName"
)
public fun <T> Is(value: T): Matcher<T> = Is.`is`(value)

public fun <T> isNot(value: T): Is<T> = Is(not(value))

public fun <T> isNot(matcher: Matcher<T>): Is<T> = Is(not(matcher))

public fun <T> anyOf(values: List<T>): Matcher<T> = anyOf(values.map { Is(it) })
