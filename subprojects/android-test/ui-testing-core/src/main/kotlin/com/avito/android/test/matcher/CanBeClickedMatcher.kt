package com.avito.android.test.matcher

import android.view.View
import com.avito.android.test.util.canHandleClick
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

internal class CanBeClickedMatcher : TypeSafeMatcher<View>() {

    override fun describeTo(description: Description) {
        description.appendText("is clickable itself or by parent")
    }

    public override fun matchesSafely(view: View): Boolean {
        return canHandleClick(view)
    }
}
