package com.avito.android.test.matcher

import android.view.View
import com.avito.android.test.util.getRect
import com.avito.android.test.util.getVisibleViewsWithHigherZOrder
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

/**
 * Check if there is an intersection of the target view's Rect and Rect of any view with higher z-order.
 *
 * How it works:
 * 1. It checks siblings of target view that are below in child list of the parent
 * 1. Check parent siblings that are below in child list of parent's parent
 * 1. Check parent's parent siblings... and so on recursively
 */
class OverlapMatcher : TypeSafeMatcher<View>() {

    override fun describeTo(description: Description) {
        description.appendText("overlapped by another views on the same screen")
    }

    override fun matchesSafely(view: View): Boolean {
        val viewRect = view.getRect()
        return view.getVisibleViewsWithHigherZOrder().any { higherZOrderView ->
            higherZOrderView.getRect().intersect(viewRect)
        }
    }
}
