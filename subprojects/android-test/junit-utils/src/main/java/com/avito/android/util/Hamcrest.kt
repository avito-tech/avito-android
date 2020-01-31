package com.avito.android.util

import org.hamcrest.Matcher
import org.hamcrest.Matchers.anyOf
import org.hamcrest.Matchers.not
import org.hamcrest.core.Is

@Suppress("HasPlatformType", "FunctionName") // unspecified return type is required to allow compiler to infer type in caller code
fun <T> Is(matcher: Matcher<T>) = Is.`is`(matcher)

@Suppress("HasPlatformType", "FunctionName") // unspecified return type is required to allow compiler to infer type in caller code
fun <T> Is(value: T) = Is.`is`(value)

fun <T> isNot(value: T) = Is(not(value))

fun <T> isNot(matcher: Matcher<T>) = Is(not(matcher))

fun <T> anyOf(values: List<T>): Matcher<T> = anyOf(values.map { Is(it) })