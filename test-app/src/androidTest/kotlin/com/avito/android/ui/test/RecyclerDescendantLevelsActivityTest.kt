package com.avito.android.ui.test

import com.avito.android.ui.RecyclerDescendantLevelsActivity
import org.junit.Rule
import org.junit.Test

class RecyclerDescendantLevelsActivityTest {

    @get:Rule
    val rule = screenRule<RecyclerDescendantLevelsActivity>()

    @Test
    fun test() {
        rule.launchActivity(null)
        Screen.recyclerDescendantLevelsScreen.apply {
            list.checks.isNotEmpty()
            list.descendantLevelOne.checks.isDisplayed()
            list.descendantLevelOne.descendantLevelTwo.checks.isDisplayed()
            list.descendantLevelOne.descendantLevelTwo.descendantLevelThree.checks.displayedWithText("Stub")
        }
    }
}

