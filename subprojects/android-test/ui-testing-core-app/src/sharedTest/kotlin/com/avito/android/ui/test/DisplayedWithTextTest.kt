package com.avito.android.ui.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.avito.android.test.app.core.screenRule
import com.avito.android.ui.IdenticalCellsRecyclerActivity
import com.avito.android.ui.R
import org.hamcrest.Matchers.startsWith
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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
            .title.checks.displayedWithText(startsWith("test"))
    }

    @Test
    fun canAssertTextWithNbsp_DisplayText() {
        rule.launchActivity(IdenticalCellsRecyclerActivity.intent(arrayListOf("test string we expect")))

        Screen.identicalCellsRecycler.list.cellAt(position = 0)
            .title.checks.displayedWithText("test string we expect")
    }

    @Test
    fun canAssertTextWithNbsp_DisplayResource() {
        rule.launchActivity(IdenticalCellsRecyclerActivity.intent(arrayListOf("test string we expect")))

        Screen.identicalCellsRecycler.list.cellAt(position = 0)
            .title.checks.displayedWithText(R.string.string_with_non_breaking_spaces)
    }
}
