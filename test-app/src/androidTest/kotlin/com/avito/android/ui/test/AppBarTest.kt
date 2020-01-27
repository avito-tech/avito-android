package com.avito.android.ui.test

import com.avito.android.ui.AppBarActivity
import org.junit.Rule
import org.junit.Test

class AppBarTest {

    @get:Rule
    val rule = screenRule<AppBarActivity>()

    @Test
    fun collapse_collapsesViews() {
        rule.launchActivity(null)

        rule.activity.setExpanded(true)

        Screen.appBarScreen.appBar.actions.collapse()
        Screen.appBarScreen.testView.checks.isNotCompletelyDisplayed()
    }

    @Test
    fun expand_showsViews() {
        rule.launchActivity(null)

        rule.activity.setExpanded(false)

        Screen.appBarScreen.appBar.actions.expand()
        Screen.appBarScreen.testView.checks.isCompletelyDisplayed()
    }
}
