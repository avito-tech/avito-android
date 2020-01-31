package com.avito.android.test.matcher

import android.view.View
import androidx.test.espresso.matcher.BoundedMatcher
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.Description
import org.hamcrest.Matcher

class TextInputPasswordVisibilityMatcher(val boolMatcher: Matcher<Boolean?>) :
    BoundedMatcher<View, TextInputLayout>(TextInputLayout::class.java) {

    override fun describeTo(description: Description) {
        description.appendText("with password visibility: ")
        boolMatcher.describeTo(description)
    }

    override fun matchesSafely(layout: TextInputLayout): Boolean {
        val passwordVisible: Boolean? = layout.javaClass.declaredFields
            .find { it.name == "mPasswordToggledVisible" }
            ?.apply { isAccessible = true }
            ?.getBoolean(layout)

        return boolMatcher.matches(passwordVisible)
    }
}
