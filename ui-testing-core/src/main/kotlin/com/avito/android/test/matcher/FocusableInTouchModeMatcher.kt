package com.avito.android.test.matcher

import android.view.View
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class FocusableInTouchModeMatcher : TypeSafeMatcher<View>() {

    override fun describeTo(description: Description) {
        description.appendText("is focusable in touch mode")
    }

    override fun matchesSafely(view: View): Boolean {
        return view.isFocusableInTouchMode
    }
}
