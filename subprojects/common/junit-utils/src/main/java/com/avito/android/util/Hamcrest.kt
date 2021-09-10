package com.avito.android.util

import org.hamcrest.Matcher
import org.hamcrest.Matchers.anyOf
import org.hamcrest.Matchers.not
import org.hamcrest.core.Is

// unspecified return type is required to allow compiler to infer type in caller code

// TODO REPLACE USAGE AND DELETE
@Suppress(
    "HasPlatformType",
    "FunctionName"
)
public fun <T> Is(matcher: Matcher<T>): Matcher<T> = Is.`is`(matcher)

// TODO REPLACE USAGE AND DELETE
@Suppress(
    "HasPlatformType",
    "FunctionName"
)
public fun <T> Is(value: T): Matcher<T> = Is.`is`(value)

// TODO DELETE
public fun <T> isNot(value: T): Is<T> = Is(not(value))

// TODO DELETE
public fun <T> isNot(matcher: Matcher<T>): Is<T> = Is(not(matcher))

// TODO DELETE
public fun <T> anyOf(values: List<T>): Matcher<T> = anyOf(values.map { Is(it) })
