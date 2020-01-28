package com.avito.android.ui.test

import android.view.MenuItem
import com.avito.android.runner.UITestFrameworkException
import com.avito.android.ui.OverflowMenuActivity
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class OverflowMenuTest {

    @get:Rule
    val rule = screenRule<OverflowMenuActivity>()

    @get:Rule
    val exception: ExpectedException = ExpectedException.none()

    @Test
    fun menuItem_isClickable_inActionMenu() {
        val label = "ACTION CLICKED"
        rule.launchActivity(OverflowMenuActivity.intent(MenuItem.SHOW_AS_ACTION_ALWAYS, label))

        Screen.overflow.toolbar.menuItem.actions.click()
        Screen.overflow.label.checks.displayedWithText(label)
    }

    @Test
    fun menuItem_isClickable_inOverflowMenu() {
        val label = "OVERFLOW CLICKED"
        rule.launchActivity(OverflowMenuActivity.intent(MenuItem.SHOW_AS_ACTION_NEVER, label))

        Screen.overflow.toolbar.menuItem.actions.click()
        Screen.overflow.label.checks.displayedWithText(label)
    }

    @Test
    fun menuItem_notFound_inOverflowMenuWithNoAutoClick() {
        rule.launchActivity(
            OverflowMenuActivity.intent(
                MenuItem.SHOW_AS_ACTION_NEVER,
                "doesn't matter"
            )
        )

        exception.expect(UITestFrameworkException::class.java)
        exception.expectMessage("Не найдена view в иерархии")

        Screen.overflow.toolbar.menuItem.withDisabledAutoOpenOverflow().actions.click()
    }
}
