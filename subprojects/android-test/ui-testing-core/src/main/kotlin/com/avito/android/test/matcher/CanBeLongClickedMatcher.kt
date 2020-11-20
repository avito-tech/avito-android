package com.avito.android.test.matcher

import android.view.View
import android.view.ViewParent
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

internal class CanBeLongClickedMatcher : TypeSafeMatcher<View>() {

    override fun describeTo(description: Description) {
        description.appendText("is long-clickable itself or by parent")
    }

    override fun matchesSafely(view: View): Boolean {
        return canHandleLongClick(view)
    }

    private fun canHandleLongClick(view: View?): Boolean {
        val parent: ViewParent? = view?.parent
        return when {
            view == null -> false
            view.isLongClickable -> true
            parent != null && parent is View -> canHandleLongClick(parent)
            else -> false
        }
    }
}
