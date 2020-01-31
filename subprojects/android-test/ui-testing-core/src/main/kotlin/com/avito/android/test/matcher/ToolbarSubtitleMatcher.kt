package com.avito.android.test.matcher

import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

internal class ToolbarSubtitleMatcher(
    private val textMatcher: Matcher<CharSequence>
) : BoundedMatcher<View, Toolbar>(Toolbar::class.java) {

    private var actualText: String? = null

    override fun describeTo(description: Description) {
        description.appendText("with toolbar subtitle: ")
        textMatcher.describeTo(description)
        when {
            actualText != null -> description.appendText(" actualText = $actualText")
        }
    }

    override fun matchesSafely(toolbar: Toolbar): Boolean {
        actualText = toolbar.subtitle.toString()
        return textMatcher.matches(actualText)
    }
}
