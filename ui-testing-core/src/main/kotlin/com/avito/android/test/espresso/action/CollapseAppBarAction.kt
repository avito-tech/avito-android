package com.avito.android.test.espresso.action

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import com.google.android.material.appbar.AppBarLayout
import org.hamcrest.Matcher

class CollapseAppBarAction : ViewAction {

    override fun getDescription() = "collapsing AppBar"

    override fun getConstraints(): Matcher<View> =
        ViewMatchers.isAssignableFrom(AppBarLayout::class.java)

    override fun perform(uiController: UiController, view: View) {
        val appBarLayout = view as AppBarLayout
        appBarLayout.setExpanded(false)
        uiController.loopMainThreadUntilIdle()
    }
}
