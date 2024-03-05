package com.avito.android.test.espresso.action

import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import com.avito.android.test.util.getFieldByReflection
import org.hamcrest.Matcher

public class ToolbarReadMenuItemsAction : ViewAction {

    private lateinit var hiddenItems: List<String>

    override fun getDescription(): String = "reading toolbar overflow menu items"

    override fun getConstraints(): Matcher<View> = isAssignableFrom(Toolbar::class.java)

    override fun perform(uiController: UiController, view: View) {
        val toolbar = view as Toolbar
        val mExpandedMenuPresenter = toolbar.getFieldByReflection<Any?>("mExpandedMenuPresenter")

        checkNotNull(mExpandedMenuPresenter) { "Cannot check overflow menu. Seems like menu is being initialized" }
        val mMenu = mExpandedMenuPresenter.getFieldByReflection<Any?>("mMenu")

        checkNotNull(mMenu) { "Cannot check overflow menu. Seems like menu is being initialized" }
        val mNonActionItems = mMenu.getFieldByReflection<ArrayList<*>>("mNonActionItems")
        hiddenItems = mNonActionItems.map { it.toString() }
    }

    public fun hasHiddenItem(itemMatcher: Matcher<String>): Boolean {
        try {
            return hiddenItems.any { itemMatcher.matches(it) }
        } catch (e: UninitializedPropertyAccessException) {
            throw ToolbarReadActionUsageException()
        }
    }

    internal class ToolbarReadActionUsageException : RuntimeException(
        """
        First of all, you should perform action, like
        "ToolbarReadMenuItemsAction()
            .apply { onView(isAssignableFrom(Toolbar::class.java)).perform(this) }"
        """.trimIndent()
    )
}
