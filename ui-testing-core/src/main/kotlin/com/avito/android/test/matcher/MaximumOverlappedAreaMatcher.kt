package com.avito.android.test.matcher

import android.graphics.Rect
import android.view.View
import com.avito.android.test.util.getAboveVisibleViews
import com.avito.android.test.util.getRect
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Modificated [androidx.test.espresso.assertion.LayoutAssertions.NoOverlapsViewAssertion.check]
 */
class MaximumOverlappedAreaMatcher(
    private val overlappingAreaPercentage: Int
) : TypeSafeMatcher<View>() {

    override fun describeTo(description: Description) {
        description.appendText("view overlapping area isn't greater then $overlappingAreaPercentage")
    }

    override fun matchesSafely(item: View): Boolean {
        val itemRect = item.getRect()
        val overlappingView = item.getAboveVisibleViews()
            .firstOrNull { view ->
                val viewRect = view.getRect()
                getFirstRectAreaIntersectPercentage(itemRect, viewRect) > overlappingAreaPercentage
            }

        return overlappingView == null
    }

    private fun getFirstRectAreaIntersectPercentage(first: Rect, second: Rect): Int {
        var areaIntersectPercentage = 0
        val intersectRectHeight = min(first.bottom, second.bottom) - max(first.top, second.top)
        val intersectRectWidth = min(first.right, second.right) - max(first.left, second.left)
        val intersectionArea = max(0, intersectRectHeight) * max(0, intersectRectWidth).toFloat()
        if (intersectionArea > 0) {
            val firstArea = (first.width() * first.height()).toFloat()
            areaIntersectPercentage = ((intersectionArea / firstArea) * 100).roundToInt()
        }
        return areaIntersectPercentage
    }
}
