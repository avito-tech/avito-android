package com.avito.android.ui.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.avito.android.test.app.core.screenRule
import com.avito.android.ui.TabLayoutActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TabLayoutTest {

    @get:Rule
    val rule = screenRule<TabLayoutActivity>(launchActivity = true)

    @Test
    fun tabsCountIs1000() {
        Screen.tabLayoutScreen.tabs.checks.withTabsCount(1000)
    }

    @Test
    fun selectTab500_tabIsDisplayed() {
        Screen.tabLayoutScreen.tabs.select(500)
        Screen.tabLayoutScreen.tabs.checks.withSelectedPosition(500)
    }
}
