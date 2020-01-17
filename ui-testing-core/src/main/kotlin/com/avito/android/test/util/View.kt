package com.avito.android.test.util

import android.view.View
import android.view.ViewGroup
import android.view.ViewParent

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
