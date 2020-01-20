package com.avito.android.test.matcher

import android.view.View
import android.widget.TextView
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

class TextViewLinesMatcher(val matcher: Matcher<Int>) :
    BoundedMatcher<View, TextView>(TextView::class.java) {

    override fun describeTo(description: Description) {
        description.appendText(" has lines count ").appendDescriptionOf(matcher)
    }

    override fun matchesSafely(item: TextView): Boolean {
        return matcher.matches(item.lineCount)
    }
}
