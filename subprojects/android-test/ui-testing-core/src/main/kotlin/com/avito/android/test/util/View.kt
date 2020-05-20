package com.avito.android.test.util

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent

/**
 * Returns list of views that have higher z-order than current view.
 * The list is ordered by ascending z-order.
 *
 * It is a more efficient analog of [TreeIterables.breadthFirstViewTraversal(this.rootView)] because
 * it iterates over less amount of views.
 */
internal fun View.getVisibleViewsWithHigherZOrder(): List<View> {
    val result = mutableListOf<View>()
    walkOverVisibleViewsWithHigherZOrder(this, result)
    return result
}

internal fun canHandleClick(view: View?): Boolean {
    val parent: ViewParent? = view?.parent
    return when {
        view == null -> false
        view.isClickable -> true
        parent != null && parent is View -> canHandleClick(parent)
        else -> false
    }
}

internal fun View.getRect(): Rect {
    val coordinates = intArrayOf(0, 0)
    getLocationOnScreen(coordinates)
    val x = coordinates[0]
    val y = coordinates[1]
    return Rect(x, y, x + width, y + height)
}

internal fun <T> View.findViewsInParent(
    viewType: Class<T>
): Collection<T> = findViewsInParentRecursively(
    startView = this,
    currentParent = this.parent,
    type = viewType
)

private fun <T> findViewsInParentRecursively(
    startView: View,
    currentParent: ViewParent,
    type: Class<T>,
    result: MutableCollection<T> = mutableSetOf()
): Collection<T> {
    if (currentParent !is ViewGroup) {
        return result
    }

    if (type.isInstance(currentParent)) {
        @Suppress("UNCHECKED_CAST")
        result += currentParent as T
    }

    (0 until currentParent.childCount)
        .map { currentParent.getChildAt(it) }
        .filter { it != startView }
        .filter { type.isInstance(it) }
        .forEach { view ->
            @Suppress("UNCHECKED_CAST")
            result += view as T
        }

    return findViewsInParentRecursively(
        startView = startView,
        currentParent = currentParent.parent,
        type = type,
        result = result
    )
}

internal fun walkOverVisibleViewsWithHigherZOrder(view: View, list: MutableList<View>) {
    val parentViewGroup = view.parent as? ViewGroup ?: return
    val index = parentViewGroup.indexOfChild(view)
    for (i in (index + 1) until parentViewGroup.childCount) {
        val child = parentViewGroup.getChildAt(i)
        if (child.visibility == View.VISIBLE) {
            list.add(child)
        }
    }

    return walkOverVisibleViewsWithHigherZOrder(parentViewGroup, list)
}
