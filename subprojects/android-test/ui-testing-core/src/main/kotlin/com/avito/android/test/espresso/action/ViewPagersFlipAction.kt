package com.avito.android.test.espresso.action

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import androidx.viewpager.widget.ViewPager
import org.hamcrest.Matcher

class ViewPagersFlipAction(private val direction: Direction) : ViewAction {

    enum class Direction { LEFT, RIGHT }

    override fun getDescription() = "Pager to $direction"

    override fun getConstraints(): Matcher<View> =
        ViewMatchers.isAssignableFrom(ViewPager::class.java)

    override fun perform(uiController: UiController, view: View) {
        val pager = view as ViewPager
        when (direction) {
            Direction.LEFT -> {
                val nextPage = pager.currentItem - 1
                if (nextPage >= 0) {
                    pager.currentItem = nextPage
                }
            }
            Direction.RIGHT -> pager.currentItem = pager.currentItem + 1
        }
    }
}
