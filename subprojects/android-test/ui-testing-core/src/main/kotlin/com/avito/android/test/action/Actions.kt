package com.avito.android.test.action

import androidx.test.espresso.action.PrecisionDescriber
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Swipe
import androidx.test.espresso.action.SwipeDirection
import androidx.test.espresso.action.Swiper

public interface Actions {

    public fun click()

    public fun longClick()

    public fun scrollTo()

    public fun swipe(
        direction: SwipeDirection,
        speed: Swiper = Swipe.FAST,
        precision: PrecisionDescriber = Press.FINGER
    )

    public fun read(allowBlank: Boolean = false): String
}
