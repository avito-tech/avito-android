package com.avito.android.ui.test

import android.view.MenuItem
import androidx.test.espresso.NoMatchingRootException
import com.avito.android.test.app.core.screenRule
import com.avito.android.ui.OverflowMenuActivity
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import ru.avito.util.assertThrows

class OverflowMenuTest {

    @get:Rule
    val rule = screenRule<OverflowMenuActivity>()

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

        val error = assertThrows<NoMatchingRootException> {
            Screen.overflow.toolbar.menuItem.withDisabledAutoOpenOverflow().actions.click()
        }
        assertThat(error).hasMessageThat().contains("Не найдена view в иерархии")
    }
}
