package com.avito.android.test.espresso.action

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import androidx.viewpager.widget.ViewPager
import org.hamcrest.Matcher

class ViewPagersSelectAction(private val tabPosition: Int) : ViewAction {

    override fun getDescription() = "selecting ViewPager"

    override fun getConstraints(): Matcher<View> =
        ViewMatchers.isAssignableFrom(ViewPager::class.java)

    override fun perform(uiController: UiController, view: View) {
        val viewPager = view as ViewPager
        viewPager.currentItem = tabPosition
        uiController.loopMainThreadUntilIdle()
    }
}
