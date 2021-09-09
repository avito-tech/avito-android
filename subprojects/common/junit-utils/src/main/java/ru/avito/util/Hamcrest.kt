package ru.avito.util

import org.hamcrest.Matcher
import org.hamcrest.core.Is
import org.hamcrest.core.IsNot

// unspecified return type is required to allow compiler to infer type in caller code

@Suppress("HasPlatformType")
public fun <T> Is(matcher: Matcher<T>): Matcher<T> = Is.`is`(matcher)

@Suppress("HasPlatformType")
public fun <T> Is(value: T): Matcher<T> = Is.`is`(value)

@Suppress("HasPlatformType")
public fun <T> IsNot(matcher: Matcher<T>): Matcher<T> = IsNot.`not`(matcher)

@Suppress("HasPlatformType")
public fun <T> IsNot(value: T): Matcher<T> = IsNot.`not`(value)
