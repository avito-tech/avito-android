package com.avito.android.ui.test

import com.avito.android.test.app.core.screenRule
import com.avito.android.ui.IdenticalCellsRecyclerActivity
import org.hamcrest.Matchers
import org.junit.Rule
import org.junit.Test

class DisplayedWithTextTest {

    @get:Rule
    val rule = screenRule<IdenticalCellsRecyclerActivity>()

    @Test
    fun canAssertText_onFoundItem() {
        rule.launchActivity(IdenticalCellsRecyclerActivity.intent(arrayListOf("test string")))

        Screen.identicalCellsRecycler.list.cellAt(position = 0)
            .title.checks.displayedWithText("test string")
    }

    @Test
    fun canAssertTextWithMatcher_onFoundItem() {
        rule.launchActivity(IdenticalCellsRecyclerActivity.intent(arrayListOf("test string")))

        Screen.identicalCellsRecycler.list.cellAt(position = 0)
            .title.checks.displayedWithText(Matchers.startsWith("test"))
    }
}
