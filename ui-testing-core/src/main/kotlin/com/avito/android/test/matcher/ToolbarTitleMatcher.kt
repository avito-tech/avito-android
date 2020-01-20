package com.avito.android.test.matcher

import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

/**
 * from http://blog.sqisland.com/2015/05/espresso-match-toolbar-title.html
 */
internal class ToolbarTitleMatcher(
    private val textMatcher: Matcher<CharSequence>
) : BoundedMatcher<View, Toolbar>(Toolbar::class.java) {

    private var actualText: String? = null

    override fun describeTo(description: Description) {
        description.appendText("with toolbar title: ")
        textMatcher.describeTo(description)
        when {
            actualText != null -> description.appendText(" actualText = $actualText")
        }
    }

    override fun matchesSafely(toolbar: Toolbar): Boolean {
        actualText = toolbar.title?.toString()
        return textMatcher.matches(actualText)
    }
}
