package com.avito.android.ui.test

import com.avito.android.ui.ButtonsOverRecyclerWithCollapsingToolbarActivity
import org.junit.Rule
import org.junit.Test

class ButtonsOverRecyclerWithCollapsingToolbarTest {

    @get:Rule
    val rule = screenRule<ButtonsOverRecyclerWithCollapsingToolbarActivity>(launchActivity = true)

    @Test
    fun listElement_elementClicked_whenThereIsOverlappedButtonInScreenWithCollapsingToolbar() {
        Screen.buttonsOverRecycler.list.cellAt(90).click()
    }
}
