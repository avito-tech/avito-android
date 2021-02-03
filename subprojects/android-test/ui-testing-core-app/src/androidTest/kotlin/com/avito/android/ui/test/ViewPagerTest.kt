package com.avito.android.ui.test

import androidx.test.espresso.action.SwipeDirections
import com.avito.android.test.app.core.screenRule
import com.avito.android.ui.ViewPagerActivity
import org.junit.Rule
import org.junit.Test

class ViewPagerTest {

    @get:Rule
    val rule = screenRule<ViewPagerActivity>()

    @Test
    fun viewPager_scrolledToRightAndLeft() {
        rule.launchActivity(null)

        repeat(10) {

            when (parityOfNumber(it)) {
                Parity.EVEN -> {
                    Screen.viewPagerScreen.pager.currentEvenPage.label.checks.isDisplayed()
                    Screen.viewPagerScreen.pager.currentOddPage.checks.doesNotExist()
                }
                Parity.ODD -> {
                    Screen.viewPagerScreen.pager.currentOddPage.label.checks.isDisplayed()
                    Screen.viewPagerScreen.pager.currentEvenPage.checks.doesNotExist()
                }
            }

            Screen.viewPagerScreen.pager.toRight()
        }

        repeat(10) {

            when (parityOfNumber(it)) {
                Parity.EVEN -> {
                    Screen.viewPagerScreen.pager.currentEvenPage.label.checks.isDisplayed()
                    Screen.viewPagerScreen.pager.currentOddPage.checks.doesNotExist()
                }
                Parity.ODD -> {
                    Screen.viewPagerScreen.pager.currentOddPage.label.checks.isDisplayed()
                    Screen.viewPagerScreen.pager.currentEvenPage.checks.doesNotExist()
                }
            }

            Screen.viewPagerScreen.pager.toLeft()
        }
    }

    @Test
    fun viewPager_scrolledToRightAndLeftUsingManualSwiping() {
        rule.launchActivity(null)

        repeat(4) {

            when (parityOfNumber(it)) {
                Parity.EVEN -> {
                    Screen.viewPagerScreen.pager.currentEvenPage.label.checks.isDisplayed()
                    Screen.viewPagerScreen.pager.currentOddPage.checks.doesNotExist()

                    Screen.viewPagerScreen.pager.currentEvenPage.swipe(SwipeDirections.RIGHT_TO_LEFT)
                }
                Parity.ODD -> {
                    Screen.viewPagerScreen.pager.currentOddPage.label.checks.isDisplayed()
                    Screen.viewPagerScreen.pager.currentEvenPage.checks.doesNotExist()

                    Screen.viewPagerScreen.pager.currentOddPage.swipe(SwipeDirections.RIGHT_TO_LEFT)
                }
            }
        }

        repeat(4) {

            when (parityOfNumber(it)) {
                Parity.EVEN -> {
                    Screen.viewPagerScreen.pager.currentEvenPage.label.checks.isDisplayed()
                    Screen.viewPagerScreen.pager.currentOddPage.checks.doesNotExist()

                    Screen.viewPagerScreen.pager.currentEvenPage.swipe(SwipeDirections.LEFT_TO_RIGHT)
                }
                Parity.ODD -> {
                    Screen.viewPagerScreen.pager.currentOddPage.label.checks.isDisplayed()
                    Screen.viewPagerScreen.pager.currentEvenPage.checks.doesNotExist()

                    Screen.viewPagerScreen.pager.currentOddPage.swipe(SwipeDirections.LEFT_TO_RIGHT)
                }
            }
        }
    }

    private fun parityOfNumber(number: Int): Parity = when (number % 2) {
        0 -> Parity.EVEN
        else -> Parity.ODD
    }

    private enum class Parity {
        EVEN,
        ODD
    }
}
