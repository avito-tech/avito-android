package com.avito.android.test.matcher

import android.graphics.RectF
import android.view.View
import androidx.test.espresso.action.CoordinatesProvider
import com.avito.android.test.util.getAboveVisibleViews
import com.avito.android.test.util.getRect
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class ViewCoordinatesNotOverlappedMatcher(
    private val coordinatesProvider: CoordinatesProvider
) : TypeSafeMatcher<View>() {

    override fun describeTo(description: Description) {
        description.appendText("view overlapped at point given by coordinatesProvider")
    }

    override fun matchesSafely(item: View): Boolean {
        val (x, y) = coordinatesProvider.calculateCoordinates(item)
        val overlappingView = item.getAboveVisibleViews()
            .filter { view -> view.isClickableOnLevelBefore(item) }
            .firstOrNull { view ->
                RectF(view.getRect()).contains(x, y)
            }

        return overlappingView == null
    }

    private fun View.isClickableOnLevelBefore(target: View): Boolean {
        val parent = parent
        return when {
            isClickable -> true
            parent is View && parent != target && parent != target.parent -> parent.isClickableOnLevelBefore(target)
            else -> false
        }
    }
}
