package com.avito.android.test.matcher

import android.view.View
import androidx.test.espresso.matcher.BoundedMatcher
import com.google.android.material.appbar.CollapsingToolbarLayout
import org.hamcrest.Description
import org.hamcrest.Matcher

internal class CollapsingToolbarTitleMatcher(private val textMatcher: Matcher<CharSequence>) :
    BoundedMatcher<View, CollapsingToolbarLayout>(CollapsingToolbarLayout::class.java) {

    override fun describeTo(description: Description) {
        description.appendText("with collapsing toolbar title: ")
        textMatcher.describeTo(description)
    }

    override fun matchesSafely(toolbarLayout: CollapsingToolbarLayout): Boolean {
        return textMatcher.matches(toolbarLayout.title)
    }
}
