package com.avito.android.test.matcher

import android.view.View
import androidx.test.espresso.matcher.BoundedMatcher
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.Description
import org.hamcrest.Matcher

internal class TextInputLayoutHintMatcher(private val stringMatcher: Matcher<String>) :
    BoundedMatcher<View, TextInputLayout>(TextInputLayout::class.java) {

    override fun describeTo(description: Description) {
        description.appendText("with hint text: ")
        stringMatcher.describeTo(description)
    }

    override fun matchesSafely(layout: TextInputLayout): Boolean =
        stringMatcher.matches(layout.hint.toString())
}
