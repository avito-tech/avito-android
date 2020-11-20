package com.avito.android.util.matcher

import org.hamcrest.Matcher
import org.hamcrest.StringDescription
import org.junit.Assert

fun <T> assertMatches(matcher: Matcher<T>, arg: T) {
    assertMatches("Expected match, but mismatched", matcher, arg)
}

fun <T> assertMatches(message: String, matcher: Matcher<T>, arg: T) {
    if (!matcher.matches(arg)) {
        Assert.fail(message + " because: '" + mismatchDescription(matcher, arg) + "'")
    }
}

fun <T> assertDoesNotMatch(matcher: Matcher<in T>, arg: T?) {
    assertDoesNotMatch("Unexpected match: $matcher, $arg", matcher, arg)
}

fun <T> assertDoesNotMatch(message: String, matcher: Matcher<in T>, arg: T?) {
    Assert.assertFalse(message, matcher.matches(arg))
}

fun assertDescription(expected: String, matcher: Matcher<*>) {
    val description = StringDescription()
    description.appendDescriptionOf(matcher)
    Assert.assertEquals("Expected description", expected, description.toString().trim())
}

fun <T> assertMismatchDescription(expected: String, matcher: Matcher<in T>, arg: T) {
    Assert.assertFalse("Precondition: Matcher should not match item.", matcher.matches(arg))
    Assert.assertEquals("Expected mismatch description", expected, mismatchDescription(matcher, arg))
}

fun assertNullSafe(matcher: Matcher<*>) {
    try {
        matcher.matches(null)
    } catch (e: Exception) {
        Assert.fail("Matcher was not null safe")
    }
}

fun assertUnknownTypeSafe(matcher: Matcher<*>) {
    try {
        matcher.matches(UnknownType())
    } catch (e: Exception) {
        Assert.fail("Matcher was not unknown type safe, because: " + e)
    }
}

private fun <T> mismatchDescription(matcher: Matcher<in T>, arg: T): String {
    val description = StringDescription()
    matcher.describeMismatch(arg, description)
    return description.toString().trim()
}

internal class UnknownType
