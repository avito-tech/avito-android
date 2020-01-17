package com.avito.android.test.matcher

import android.view.View
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

class AlphaMatcher(val matcher: Matcher<Float>) : TypeSafeMatcher<View>() {

    override fun describeTo(description: Description) {
        description.appendText(" has alpha ").appendDescriptionOf(matcher)
    }

    override fun matchesSafely(item: View): Boolean {
        return matcher.matches(item.alpha)
    }
}
