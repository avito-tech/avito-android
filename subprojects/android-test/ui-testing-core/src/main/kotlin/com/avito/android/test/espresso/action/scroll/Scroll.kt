package com.avito.android.test.espresso.action.scroll

import android.graphics.Point
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import com.avito.android.test.util.findViewsInParent
import com.google.android.material.appbar.AppBarLayout

internal fun View.collapseAllAppBarsInParent() {
    findViewsInParent(AppBarLayout::class.java)
        .forEach { it.setExpanded(false) }
}

internal fun View.scrollToScrollableParentCenterPosition() {
    val parent = getScrollableContainer(this)

    val center = Point(
        parent.width / 2,
        parent.height / 2
    )

    scrollToScrollableParentPosition(position = center)
}

internal fun View.scrollToScrollableParentPosition(position: Point) {
    val parent = getScrollableContainer(this)

    val viewPositionInsideContainer = getPositionInScrollableParent()

    val x = viewPositionInsideContainer.left - position.x + width / 2
    val y = viewPositionInsideContainer.top - position.y + height / 2

    parent.scrollBy(x, y)
}

private fun getScrollableContainer(view: View): ViewGroup {
    if (view is ViewGroup && view.isScrollContainer) {
        return view
    }

    if (view.parent !is ViewGroup) {
        throw IllegalStateException("Scrollable container not found for view")
    }

    return getScrollableContainer(view.parent as View)
}

private fun View.getPositionInScrollableParent(): Rect {
    val result = Rect()

    val left = getRelativeToFirstScrollableContainerLeft(this)
    val right = left + width
    val top = getRelativeToFirstScrollableContainerTop(this)
    val bottom = top + height

    result.set(
        left,
        top,
        right,
        bottom
    )

    return result
}

private fun getRelativeToFirstScrollableContainerLeft(view: View): Int {
    if (view.parent !is ViewGroup) {
        throw RuntimeException(
            "Failed get left coordinate relative to first scrollable " +
                "parent because parent isn't view group"
        )
    }

    if (view is ViewGroup && view.isScrollContainer) {
        return 0
    }

    return view.left + getRelativeToFirstScrollableContainerLeft(
        view.parent as View
    )
}

private fun getRelativeToFirstScrollableContainerTop(view: View): Int {
    if (view.parent !is ViewGroup) {
        throw RuntimeException(
            "Failed get left coordinate relative to first scrollable " +
                "parent because parent isn't view group"
        )
    }

    if (view is ViewGroup && view.isScrollContainer) {
        return 0
    }

    return view.top + getRelativeToFirstScrollableContainerTop(
        view.parent as View
    )
}
