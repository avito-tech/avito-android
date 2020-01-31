package com.avito.android.ui.test

import com.avito.android.ui.MovingButtonActivity
import org.junit.Rule
import org.junit.Test

class MovingButtonTest {

    @get:Rule
    val rule = screenRule<MovingButtonActivity>(launchActivity = true)

    @Test
    fun movingButton_clicked() {
        Screen.movingButton.movedButton.click()
        Screen.movingButton.movedButtonClickIndicatorView.checks.isDisplayed()
    }
}
