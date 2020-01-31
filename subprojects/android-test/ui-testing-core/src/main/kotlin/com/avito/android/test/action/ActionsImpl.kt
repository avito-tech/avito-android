package com.avito.android.test.action

import androidx.test.espresso.action.PrecisionDescriber
import androidx.test.espresso.action.SwipeDirection
import androidx.test.espresso.action.Swiper
import com.avito.android.test.espresso.EspressoActions
import com.avito.android.test.espresso.action.TextViewReadAction

class ActionsImpl(private val driver: ActionsDriver) : Actions {

    override fun click() {
        driver.perform(
            EspressoActions.scrollIfPossible(),
            EspressoActions.click()
        )
    }

    override fun longClick() {
        driver.perform(
            EspressoActions.scrollIfPossible(),
            EspressoActions.longClick()
        )
    }

    override fun scrollTo() {
        driver.perform(EspressoActions.scrollIfPossible())
    }

    override fun swipe(direction: SwipeDirection, speed: Swiper, precision: PrecisionDescriber) {
        driver.perform(EspressoActions.swipe(direction, speed, precision))

        // FIXME
        Thread.sleep(1000)
    }

    override fun read(allowBlank: Boolean): String =
        TextViewReadAction.getResult(allowBlank) { action -> driver.perform(action) }
}
