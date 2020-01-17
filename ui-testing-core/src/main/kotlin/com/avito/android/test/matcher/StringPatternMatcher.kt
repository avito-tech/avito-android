package com.avito.android.test.matcher

import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import java.util.regex.Pattern

class StringPatternMatcher(private val pattern: Pattern) : TypeSafeMatcher<String>() {

    override fun matchesSafely(item: String): Boolean {
        return pattern.matcher(item).find()
    }

    override fun describeMismatchSafely(item: String, mismatchDescription: Description) {
        mismatchDescription.appendText("a string $item does not match pattern")
            .appendText(pattern.toString())
    }

    override fun describeTo(description: Description) {
        description.appendText("matching pattern ")
            .appendText(pattern.toString())
    }
}
