package com.avito.android.test.matcher

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
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
        val viewRect = Rect()
        view.getGlobalVisibleRect(viewRect)

        return view.parentViewGroup?.iterateViewsBelow(view) { viewBelow ->
            viewBelow.isIntersectedWith(viewRect)
        } ?: false
    }

    private fun ViewGroup.iterateViewsBelow(view: View, block: (View) -> Boolean): Boolean {
        val index = indexOfChild(view)
        for (i in (index + 1) until childCount) {
            val child = getChildAt(i)
            if (block(child)) {
                return true
            }
        }

        return parentViewGroup?.iterateViewsBelow(this, block) ?: false
    }

    private fun View.isIntersectedWith(otherRect: Rect): Boolean {
        val thisRect = Rect()
        getGlobalVisibleRect(thisRect)
        return thisRect.intersect(otherRect)
    }

    private val View.parentViewGroup: ViewGroup?
        get() = parent as? ViewGroup
}
