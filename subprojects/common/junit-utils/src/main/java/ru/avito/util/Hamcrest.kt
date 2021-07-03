package ru.avito.util

import org.hamcrest.Matcher
import org.hamcrest.core.Is
import org.hamcrest.core.IsNot

// unspecified return type is required to allow compiler to infer type in caller code

@Suppress("HasPlatformType")
fun <T> Is(matcher: Matcher<T>) = Is.`is`(matcher)

@Suppress("HasPlatformType")
fun <T> Is(value: T) = Is.`is`(value)

@Suppress("HasPlatformType")
fun <T> IsNot(matcher: Matcher<T>) = IsNot.`not`(matcher)

@Suppress("HasPlatformType")
fun <T> IsNot(value: T) = IsNot.`not`(value)
