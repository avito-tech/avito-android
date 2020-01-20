package com.avito.android.test.matcher

import android.annotation.SuppressLint
import android.view.MenuItem
import android.view.View
import android.widget.Checkable
import androidx.appcompat.view.menu.ActionMenuItemView
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

/**
 * High-level checked matcher, anything that somehow can be "checked" must be handled by this matcher
 */
class UniversalCheckedMatcher(private val checkedStateMatcher: Matcher<Boolean>) :
    TypeSafeMatcher<View>() {

    override fun describeTo(description: Description) {
        description.appendText("with checked state: ")
        checkedStateMatcher.describeTo(description)
    }

    @SuppressLint("RestrictedApi")
    override fun matchesSafely(item: View): Boolean =
        checkedStateMatcher.matches(
            when (item) {
                is Checkable -> item.isChecked
                is MenuItem -> item.isChecked
                is ActionMenuItemView -> item.itemData.isChecked
                else -> false
            }
        )
}
