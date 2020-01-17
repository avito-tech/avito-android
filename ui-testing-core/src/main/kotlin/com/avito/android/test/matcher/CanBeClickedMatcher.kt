package com.avito.android.test.matcher

import android.view.View
import android.view.ViewParent
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

internal class CanBeClickedMatcher : TypeSafeMatcher<View>() {

    override fun describeTo(description: Description) {
        description.appendText("is clickable itself or by parent")
    }

    public override fun matchesSafely(view: View): Boolean {
        return canHandleClick(view)
    }

    private fun canHandleClick(view: View?): Boolean {
        val parent: ViewParent? = view?.parent
        return when {
            view == null -> false
            view.isClickable -> true
            parent != null && parent is View -> canHandleClick(parent)
            else -> false
        }
    }
}