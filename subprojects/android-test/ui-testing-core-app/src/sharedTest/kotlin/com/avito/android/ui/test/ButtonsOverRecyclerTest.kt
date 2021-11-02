package com.avito.android.ui.test

import androidx.test.espresso.action.SwipeDirections
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.avito.android.test.app.core.screenRule
import com.avito.android.ui.ButtonsOverRecyclerActivity
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThan
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ButtonsOverRecyclerTest {

    @get:Rule
    val rule = screenRule<ButtonsOverRecyclerActivity>(launchActivity = true)

    @Test
    fun listElement_swipe_RecyclerViewBehindButtons() {
        with(Screen.buttonsOverRecycler.list) {
            actions.swipe(SwipeDirections.BOTTOM_TO_TOP)
            checks.firstVisiblePosition(greaterThan(0))
            // on some devices swipe to top may end on half way
            repeat(3) { actions.swipe(SwipeDirections.TOP_TO_BOTTOM) }
            checks.firstVisiblePosition(equalTo(0))
        }
    }

    @Test
    fun listElement_elementClicked_whenThereIsOverlappedButton() {
        Screen.buttonsOverRecycler.list.cellAt(60).click()
    }
}
