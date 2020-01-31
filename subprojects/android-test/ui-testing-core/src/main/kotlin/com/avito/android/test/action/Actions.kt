package com.avito.android.test.action

import androidx.test.espresso.action.PrecisionDescriber
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Swipe
import androidx.test.espresso.action.SwipeDirection
import androidx.test.espresso.action.Swiper

interface Actions {

    fun click()

    fun longClick()

    fun scrollTo()

    fun swipe(
        direction: SwipeDirection,
        speed: Swiper = Swipe.FAST,
        precision: PrecisionDescriber = Press.FINGER
    )

    fun read(allowBlank: Boolean = false): String
}
