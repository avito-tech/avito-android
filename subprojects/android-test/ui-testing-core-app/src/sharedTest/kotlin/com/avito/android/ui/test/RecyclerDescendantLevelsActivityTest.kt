package com.avito.android.ui.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.avito.android.test.app.core.screenRule
import com.avito.android.ui.RecyclerDescendantLevelsActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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
